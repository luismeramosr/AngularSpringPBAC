package com.example.pbac.web.middleware;

import com.example.pbac.persistence.service.security.UserService;
import com.example.pbac.web.security.Error;
import com.example.pbac.web.security.ErrorKind;
import com.example.pbac.web.security.Result;
import com.example.pbac.web.security.service.JwtService;
import com.example.pbac.web.security.service.JwtService.ClaimWrapper;
import com.example.pbac.web.security.service.JwtService.ExpiredWrapper;
import com.example.pbac.web.security.service.JwtService.ValidationWrapper;

import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Filtro de autenticación basado en JWT que se ejecuta una vez por cada
 * solicitud.
 * Verifica si el token JWT es válido, si no está expirado, y si pertenece a un
 * usuario autenticado.
 *
 * Este filtro se activa automáticamente gracias a la anotación `@Component` y
 * usa dependencias proporcionadas a través de la inyección de constructor,
 * habilitada por `@RequiredArgsConstructor`.
 */

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserService userService;

    /**
     * Realiza la lógica del filtro para autenticar usuarios basándose en un token
     * JWT.
     *
     * Si la solicitud corresponde a rutas específicas (e.g., `/auth/signin`,
     * `/auth/refresh_token`),
     * el filtro permite la solicitud sin validación adicional.
     *
     * Si un token JWT está presente, valida su expiración, extrae el nombre de
     * usuario,
     * y, si es válido, establece la autenticación en el contexto de seguridad de
     * Spring.
     *
     * @param request  Solicitud HTTP entrante.
     * @param response Respuesta HTTP.
     * @param filter   Cadena de filtros donde se aplica este filtro.
     * @throws ServletException Si ocurre un error relacionado con el servlet.
     * @throws IOException      Si ocurre un error de E/S durante el procesamiento.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filter)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        boolean isSigningIn = ((HttpServletRequest) request).getRequestURL().toString().contains("/auth/login");
        boolean isRefreshingToken = ((HttpServletRequest) request).getRequestURL().toString()
                .contains("/auth/refresh_token");

        if (isSigningIn || isRefreshingToken) {
            filter.doFilter(request, response);
            return;
        }

        if (authHeader == null) {
            filter.doFilter(request, response);
            return;
        }
        jwt = authHeader.substring(7);

        // Por alguna razón lo primero que hace Spring de forma automática es
        // validar que el token no haya expirado, por eso valido eso mismo aqui primero
        // de lo contrario arrojaria el mismo tipo de error ("ExpiredTokenError")
        // al obtener el username
        ExpiredWrapper tokenExpiredWrapper = jwtService.isTokenExpired(jwt);
        if (tokenExpiredWrapper.isExpired()) {
            logger.error(String.format("Error: %s", tokenExpiredWrapper.getErr().getMessage()));
            setResultError(request, response, filter, tokenExpiredWrapper.getErr());
            return;
        }

        ClaimWrapper<String> usernameWrapper = jwtService.extractUsername(jwt);
        if (usernameWrapper.getClaim().isEmpty()) {
            setResultError(request, response, filter, usernameWrapper.getErr());
            return;
        }

        if (StringUtils.isNotEmpty(usernameWrapper.getClaim())
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userService.userDetailsService()
                    .loadUserByUsername(usernameWrapper.getClaim());

            ValidationWrapper wrapper = jwtService.isTokenValid(jwt, userDetails);
            if (wrapper.isValid()) {
                SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
                token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                securityContext.setAuthentication(token);
                SecurityContextHolder.setContext(securityContext);
                filter.doFilter(request, response);
            } else {
                setResultError(request, response, filter, wrapper.getErr());
                return;
            }
        }
    }

    /**
     * Configura una respuesta HTTP de error en caso de que ocurra un problema
     * durante la validación del token.
     *
     * @param request  Solicitud HTTP.
     * @param response Respuesta HTTP.
     * @param filter   Cadena de filtros.
     * @param err      Objeto de error que detalla el problema ocurrido.
     * @throws ServletException Si ocurre un error relacionado con el servlet.
     * @throws IOException      Si ocurre un error de E/S durante el procesamiento.
     */
    private void setResultError(HttpServletRequest request, HttpServletResponse response, FilterChain filter,
            Error err) throws ServletException, IOException {
        Result<Void, Error> result = new Result<Void, Error>();
        result.setOk(null);
        result.setErr(err);
        var writer = response.getWriter();
        response.setHeader("Content-Type", "application/json");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        writer.write(result.toJson());
        writer.flush();
        writer.close();
    }
}
