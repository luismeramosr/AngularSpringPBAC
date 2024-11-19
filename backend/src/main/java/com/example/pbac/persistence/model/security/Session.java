package com.example.pbac.persistence.model.security;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

/**
 * Este archivo genera la tabla de Sesión. Su funcion es almacenar y manejar el
 * token generado una vez
 * que se inicia sesion en el sistema. Contiene los valores del Token de Acceso
 * y el Token para refrescar
 * el Token de Acceso.
 */

@Data
@Entity
public class Session {
    @Id
    private String refreshToken;
    private String accessToken;
}
