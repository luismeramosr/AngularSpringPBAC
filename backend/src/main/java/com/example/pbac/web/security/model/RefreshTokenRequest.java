package com.example.pbac.web.security.model;

import lombok.Data;

/**
 * RefreshTokenRequest representa la estructura de los datos enviados por un
 * cliente
 * para solicitar un nuevo token de acceso utilizando un token de actualización.
 *
 * Contiene únicamente el token de actualización necesario para la operación.
 */

@Data
public class RefreshTokenRequest {
    private String refreshToken;
}
