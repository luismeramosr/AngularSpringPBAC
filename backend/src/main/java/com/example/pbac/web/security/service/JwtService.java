package com.example.pbac.web.security.service;

import com.example.pbac.util.config.Config;
import com.example.pbac.web.security.Error;
import com.example.pbac.web.security.ErrorKind;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * JwtService gestiona la generación, validación y extracción de información de
 * tokens JWT.
 * Proporciona métodos para trabajar con tokens de acceso y tokens de
 * actualización, y utiliza
 * varias clases auxiliares para encapsular los resultados de las operaciones.
 */

@Service
public class JwtService {

    /**
     * Encapsula las claims (declaraciones) extraídas de un token JWT y un posible
     * error.
     * Su funcion es para representar el resultado de la extracción de claims de un
     * token,
     * con un posible error.
     */
    @Data
    public class ClaimsWrapper {
        private Claims claims;
        private Error err;
    }

    /**
     * Envuelve un claim específico (de tipo genérico T) extraído de un token, junto
     * con un
     * error asociado. Su funcion es para manejar datos extraídos del token con
     * flexibilidad,
     * sin importar el tipo de datos (String, Date, etc.).
     */
    @Data
    public class ClaimWrapper<T> {
        private T claim;
        private Error err;
    }

    /**
     * Representa el resultado de la validación de un token JWT.
     * Su funcion es para verificar si un token es válido en función
     * de criterios específicos, como la correspondencia con un usuario o la firma.
     */
    @Data
    public class ValidationWrapper {
        private boolean isValid;
        private Error err;
    }

    /**
     * Indica si un token JWT ha expirado.
     * Su funcion es para gestionar la expiración de un token y proporcionar
     * detalles
     * adicionales en caso de que esté expirado.
     */
    @Data
    public class ExpiredWrapper {
        private boolean isExpired;
        private Error err;
    }

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Autowired
    private Config config;

    /**
     * Genera un token JWT de acceso para un usuario dado.
     *
     * @param userDetails Detalles del usuario, incluido su nombre de usuario.
     * @return Token JWT generado.
     */
    public String generateAccessToken(UserDetails userDetails) {
        String token = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + config.expiration_millis))
                .signWith(getSiginKey(), SignatureAlgorithm.HS256)
                .compact();
        logger.info("Generated JWT: {}", token);
        return token;
    }

    /**
     * Genera un token de actualización único.
     *
     * @return Token de actualización generado como un UUID.
     */
    public String generateRefreshToken() {
        String refreshToken = UUID.randomUUID().toString();
        logger.info("Generated Refresh Token: {}", refreshToken);
        return refreshToken;
    }

    /**
     * Extrae el nombre de usuario del token JWT.
     *
     * @param token Token JWT del cual extraer el nombre de usuario.
     * @return Un `ClaimWrapper` que contiene el nombre de usuario o un error si
     *         ocurre algún problema.
     */
    public ClaimWrapper<String> extractUsername(String token) {
        logger.info("Extracting username from token: {}", token);
        ClaimWrapper<String> wrapper = extractClaim(token, Claims::getSubject);
        return wrapper;
    }

    /**
     * Extrae un *claim* específico del token JWT utilizando una función de
     * resolución.
     *
     * @param <T>            Tipo del *claim* a extraer.
     * @param token          Token JWT del cual extraer el *claim*.
     * @param claimsResolver Función para resolver el *claim* deseado.
     * @return Un `ClaimWrapper` que contiene el *claim* extraído o un error si
     *         ocurre algún problema.
     */
    private <T> ClaimWrapper<T> extractClaim(String token, Function<Claims, T> claimsResolver) {
        var result = extractAllClaims(token);
        Claims claims = result.getClaims();
        if (result.getErr() != null) {
            ClaimWrapper<T> wrapper = new ClaimWrapper<T>();
            wrapper.setClaim(claimsResolver.apply(claims));
            wrapper.setErr(result.getErr());
            return wrapper;
        } else {
            ClaimWrapper<T> wrapper = new ClaimWrapper<T>();
            wrapper.setClaim(claimsResolver.apply(claims));
            return wrapper;
        }
    }

    /**
     * Obtiene la clave de firma utilizada para firmar y validar tokens JWT.
     *
     * @return Clave criptográfica derivada de la clave secreta configurada.
     */
    private Key getSiginKey() {
        byte[] key = Decoders.BASE64.decode(config.secret_key);
        return Keys.hmacShaKeyFor(key);
    }

    /**
     * Extrae todas las declaraciones (*claims*) de un token JWT.
     *
     * @param token Token JWT del cual extraer las declaraciones.
     * @return Un `ClaimsWrapper` con las declaraciones extraídas o un error si
     *         ocurre algún problema.
     */
    private ClaimsWrapper extractAllClaims(String token) {
        ClaimsWrapper wrapper = new ClaimsWrapper();
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(getSiginKey()).build().parseClaimsJws(token).getBody();
            wrapper.setClaims(claims);
            return wrapper;
        } catch (ExpiredJwtException err) {
            wrapper.setClaims(err.getClaims());
            wrapper.setErr(new Error("La sesión ha expirado", String.format("JWT has expired -> %s", err.getMessage()),
                    ErrorKind.ExpiredTokenError));
            return wrapper;
        }
    }

    /**
     * Valida si un token JWT es válido para un usuario específico.
     *
     * @param token       Token JWT a validar.
     * @param userDetails Detalles del usuario a validar contra el token.
     * @return Un `ValidationWrapper` indicando si el token es válido o no.
     */
    public ValidationWrapper isTokenValid(String token, UserDetails userDetails) {
        ClaimWrapper<String> username = extractUsername(token);
        if (username.getClaim().isEmpty()) {
            ValidationWrapper wrapper = new ValidationWrapper();
            wrapper.setValid(false);
            wrapper.setErr(
                    new Error("El usuario no existe", username.getErr().getMessage(),
                            ErrorKind.NoUsernameInTokenError));
            return wrapper;
        }

        logger.info("Extracted username: {}", username);
        boolean usernameIsValid = username.getClaim().equals(userDetails.getUsername());
        ValidationWrapper wrapper = new ValidationWrapper();
        wrapper.setValid(usernameIsValid);
        return wrapper;
    }

    /**
     * Verifica si un token JWT ha expirado.
     *
     * @param token Token JWT a verificar.
     * @return Un `ExpiredWrapper` indicando si el token está expirado.
     */
    public ExpiredWrapper isTokenExpired(String token) {
        ClaimWrapper<Date> wrapper = extractClaim(token, Claims::getExpiration);
        ExpiredWrapper expiredWrapper = new ExpiredWrapper();
        expiredWrapper.setExpired(wrapper.getClaim().before(new Date()));
        expiredWrapper.setErr(wrapper.getErr());
        return expiredWrapper;
    }
}
