package com.example.pbac.web.dto;

import java.util.Optional;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * UserDto es un objeto de transferencia de datos que encapsula la información
 * necesaria
 * para ser usada en la capa de servicio y controlador de Autenticación entre
 * otros.
 * Contiene el ID, username y email
 * Utiliza anotaciones de validación para garantizar que ciertos campos sean
 * válidos.
 */

@Data
public class UserDto {
    @NotBlank
    private Long user_id;
    @NotBlank
    private String username;
    @NotBlank
    private String email;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
}
