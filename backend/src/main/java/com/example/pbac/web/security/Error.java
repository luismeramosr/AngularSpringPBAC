package com.example.pbac.web.security;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Error representa un objeto que encapsula detalles sobre un error ocurrido
 * durante una operación.
 *
 * Proporciona un mensaje descriptivo del error, la causa subyacente y el tipo
 * de error,
 * lo que facilita la depuración y el manejo de errores en la aplicación.
 * Este Error se manda junto al Result y llega a ocurrir.
 */

@Data
@AllArgsConstructor
public class Error {
    private String message;
    private String cause;
    private ErrorKind errorKind;
}
