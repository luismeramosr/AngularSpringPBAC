package com.example.pbac.web.security.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * JwtAuthResponse encapsula los tokens de autenticación que se retornan
 * al cliente después de un inicio de sesión exitoso.
 *
 * Contiene el token de acceso y el token de actualización necesarios
 * para interactuar con los recursos protegidos de la aplicación y
 * renovar la autenticación cuando sea necesario.
 */

@Data
public class JwtAuthResponse {
    @NotBlank
    private String accessToken;
    @NotBlank
    private String refreshToken;
}
