package com.example.pbac.persistence.service.security;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.example.pbac.persistence.model.security.Role;
import com.example.pbac.persistence.model.security.Session;
import com.example.pbac.persistence.model.security.User;
import com.example.pbac.util.config.Config;
import com.example.pbac.web.dto.UserDto;
import com.example.pbac.web.security.Error;
import com.example.pbac.web.security.ErrorKind;
import com.example.pbac.web.security.Result;
import com.example.pbac.web.security.model.JwtAuthResponse;
import com.example.pbac.web.security.model.LoginRequest;
import com.example.pbac.web.security.model.RefreshTokenRequest;
import com.example.pbac.web.security.model.UserFactory;
import com.example.pbac.web.security.provider.PasswordEncoderProvider;
import com.example.pbac.web.security.service.JwtService;
import com.example.pbac.web.security.service.JwtService.ClaimWrapper;

import lombok.RequiredArgsConstructor;

/**
 * Este archivo es el encargado de la gestión de Autenticación (Login, Register,
 * Refresh Token, etc)
 * Para la creacion de estos metodos ocupamos los metodos creados en la capa
 * repository.
 * El servicio maneja las respuestas con el tipo `Result` que
 * encapsula
 * tanto el valor correcto como el error (si ocurre).
 */

@Service
@RequiredArgsConstructor
public class AuthService {
    private final Config config;
    private final UserService userService;
    private final SessionService sessionService;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final PasswordEncoderProvider passwordEncoderProvider;

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    /**
     * Inicia sesión de un usuario y genera tokens JWT.
     *
     * Este método autentica a un usuario utilizando su nombre de usuario y
     * contraseña,
     * y genera un token de acceso y un token de actualización (refresh token).
     * También maneja la activación de la cuenta del usuario si es necesario.
     *
     * @param request Un objeto `LoginRequest` que contiene el nombre de usuario y
     *                la contraseña del usuario.
     * @return Un objeto `Result` que contiene un `JwtAuthResponse` con los tokens
     *         generados o un error si la autenticación falla.
     */
    public Result<JwtAuthResponse, Error> login(LoginRequest request) {
        Result<JwtAuthResponse, Error> result = new Result<>();
        var _user = userService.findByUsername(request.getUsername());
        if (_user.isEmpty()) {
            result.setErr(new Error(String.format("El usuario %s no existe", request.getUsername()),
                    String.format("User %s not found", request.getUsername()),
                    ErrorKind.RepositoryError));
            return result;
        }
        User user = _user.get();

        if (!user.isActive()) {
            result.setErr(new Error("El usuario no esta activo", "User is not active",
                    ErrorKind.ServiceError));
            return result;
        }

        if (!passwordEncoderProvider.passwordEncoder().matches(request.getPassword(), user.getPassword())) {
            result.setErr(new Error("Contraseña incorrecta", "Passwords do not match",
                    ErrorKind.ServiceError));
            return result;
        }

        // Autenticar al usuario
        authManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        var jwt = jwtService.generateAccessToken(new UserFactory(user));
        var refreshToken = jwtService.generateRefreshToken();

        JwtAuthResponse response = new JwtAuthResponse();
        response.setAccessToken(jwt);
        response.setRefreshToken(refreshToken);
        Session session = new Session();
        session.setAccessToken(jwt);
        session.setRefreshToken(refreshToken);
        sessionService.upsert(session, refreshToken);
        result.setOk(response);
        return result;
    }

    /**
     * Refresca el token de acceso utilizando un token de actualización.
     *
     * Este método valida la sesión utilizando el token de actualización
     * proporcionado,
     * y genera nuevos tokens de acceso y de actualización si la sesión es válida.
     *
     * @param request Un objeto `RefreshTokenRequest` que contiene el token de
     *                actualización.
     * @return Un objeto `Result` que contiene un `JwtAuthResponse` con los nuevos
     *         tokens generados o un error si la sesión no es válida.
     */
    public Result<JwtAuthResponse, Error> refreshToken(RefreshTokenRequest request) {
        Result<JwtAuthResponse, Error> result = new Result<>();
        Optional<Session> _session = sessionService.findById(request.getRefreshToken());
        if (_session.isEmpty()) {
            result.setErr(new Error("No hay una sesión valida.",
                    "No se encontró una sesión que corresponda al token de refresco propocionado",
                    ErrorKind.RepositoryError));
            return result;
        }

        String userEmail = jwtService.extractUsername(_session.get().getAccessToken()).getClaim();
        Optional<User> _user = userService.findByUsername(userEmail);
        if (_user.isEmpty()) {
            result.setErr(new Error("No se encontró el usuario.",
                    "No se encontró un usuario en el token de acceso proporcionado",
                    ErrorKind.RepositoryError));
            return result;
        }

        UserFactory user = new UserFactory(_user.get());

        if (jwtService.isTokenValid(_session.get().getAccessToken(), user).isValid()) {
            var jwt = jwtService.generateAccessToken(user);
            var refreshToken = jwtService.generateRefreshToken();

            JwtAuthResponse response = new JwtAuthResponse();
            response.setAccessToken(jwt);
            response.setRefreshToken(refreshToken);
            Session session = new Session();
            session.setAccessToken(jwt);
            session.setRefreshToken(refreshToken);
            sessionService.upsert(session, request.getRefreshToken());
            result.setOk(response);
            return result;
        } else {
            result.setErr(new Error("No se encontró el usuario.",
                    "El token está comprometido / modificado, no es válido",
                    ErrorKind.RepositoryError));
            return result;
        }
    }

    /**
     * Recupera el usuario a partir de un token JWT.
     *
     * Este método extrae el nombre de usuario del token JWT proporcionado, luego
     * busca al usuario
     * en la base de datos utilizando ese nombre de usuario. Si el usuario es
     * encontrado, se devuelve
     * un objeto `UserDto` con los detalles del usuario.
     *
     * @param token El token JWT del cual se extrae el nombre de usuario para buscar
     *              al usuario.
     * @return Un objeto `Result` que contiene un `UserDto` con la información del
     *         usuario, o un error si no se encuentra el usuario.
     */
    public Result<UserDto, Error> getUserFromToken(String token) {
        Result<UserDto, Error> result = new Result<>();
        token = token.substring(7);
        ClaimWrapper<String> username = jwtService.extractUsername(token);

        Optional<User> _user = userService.findByUsername(username.getClaim());
        if (_user.isEmpty()) {
            result.setErr(new Error("No se encontró el usuario.",
                    "No se encontro el usuario segun sus claims",
                    ErrorKind.RepositoryError));
            return result;
        }

        UserDto user = new UserDto();
        user.setUser_id(_user.get().getId());
        user.setUsername(_user.get().getUsername());
        user.setEmail(_user.get().getEmail());

        result.setOk(user);
        return result;
    }

    public Result<Boolean, Error> isAuthorized(String accessToken, String role) {
        Result<Boolean, Error> result = new Result<>();
        String username = jwtService.extractUsername(accessToken.substring(7)).getClaim();
        Optional<User> _user = userService.findByUsername(username);

        if (_user.isEmpty()) {
            result.setErr(new Error("No se encontró el usuario.",
                    "No se encontro el usuario segun sus claims",
                    ErrorKind.RepositoryError));
            return result;
        }

        result.setOk(false);
        for (Role r : _user.get().getRoles()) {
            logger.warn(r.getName());
            logger.warn(role);
            if (r.getName().equals(role)) {
                result.setOk(true);
            }
        }

        return result;
    }
}
