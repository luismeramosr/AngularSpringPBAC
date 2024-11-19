package com.example.pbac.web.security.model;

import lombok.Data;

/**
 * LoginRequest representa la estructura de los datos enviados por un cliente
 * para realizar el proceso de inicio de sesión en la aplicación.
 *
 * Contiene las credenciales del usuario necesarias para autenticarlo.
 */

@Data
public class LoginRequest {
    String username;
    String password;
}
