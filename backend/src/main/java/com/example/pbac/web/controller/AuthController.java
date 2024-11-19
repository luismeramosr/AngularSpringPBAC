package com.example.pbac.web.controller;

import org.springframework.web.bind.annotation.RestController;

import com.example.pbac.persistence.model.security.Session;
import com.example.pbac.persistence.model.security.User;
import com.example.pbac.persistence.service.security.AuthService;
import com.example.pbac.persistence.service.security.SessionService;
import com.example.pbac.persistence.service.security.UserService;
import com.example.pbac.web.security.Result;
import com.example.pbac.web.dto.UserDto;
import com.example.pbac.web.security.Error;
import com.example.pbac.web.security.ErrorKind;
import com.example.pbac.web.security.model.JwtAuthResponse;
import com.example.pbac.web.security.model.LoginRequest;
import com.example.pbac.web.security.model.RefreshTokenRequest;
import com.example.pbac.web.security.model.UserFactory;
import com.example.pbac.web.security.service.JwtService;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    /**
     * Inicia sesión en el sistema y genera un JWT.
     *
     * @param request Objeto `LoginRequest` con las credenciales de inicio de
     *                sesión.
     * @return Un objeto `Result` con el JWT y la información del usuario
     *         autenticado, o un error si la operación falla.
     */
    @PostMapping("/login")
    public ResponseEntity<Result<JwtAuthResponse, Error>> login(@RequestBody LoginRequest request) {
        Result<JwtAuthResponse, Error> result = authService.login(request);
        if (result.getErr() != null) {
            return new ResponseEntity<>(result,
                    result.getErr().getErrorKind() == ErrorKind.RepositoryError ? HttpStatus.BAD_REQUEST
                            : HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Refresca el token JWT de un usuario.
     *
     * @param request Objeto `RefreshTokenRequest` que contiene el refresh token.
     * @return Un objeto `Result` con el nuevo JWT, o un error si la operación
     *         falla.
     */
    @PostMapping("/refresh_token")
    public ResponseEntity<Result<JwtAuthResponse, Error>> refreshToken(@RequestBody RefreshTokenRequest request) {
        logger.info(String.format("RefreshToken: %s", request.getRefreshToken()));

        Result<JwtAuthResponse, Error> result = authService.refreshToken(request);
        if (result.getErr() != null) {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Obtiene los datos del usuario a partir del token JWT.
     *
     * @param token El token de autenticación que se pasa en el encabezado de la
     *              solicitud.
     * @return Un objeto `Result` con los datos del usuario, o un error si la
     *         operación falla.
     */
    @GetMapping("/user")
    public ResponseEntity<Result<UserDto, Error>> getUserFromToken(@RequestHeader("Authorization") String token) {
        Result<UserDto, Error> result = authService.getUserFromToken(token);
        if (result.getErr() != null) {
            return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
        }
        logger.info(String.format("User -> %s", result.getOk().getUsername()));
        return ResponseEntity.ok(result);
    }
}
