package com.example.pbac.web.security;

/**
 * ErrorKind es un enumerado que representa los diferentes tipos de errores que
 * pueden ocurrir
 * en la aplicación, clasificados según el componente o capa donde se produjo el
 * fallo.
 *
 * Esta enumeración se utiliza para categorizar los errores y facilitar su
 * manejo adecuado.
 */

public enum ErrorKind {
    RepositoryError,
    ServiceError,
    SpecificationError,
    ControllerError,
    NoUsernameInTokenError,
    ExpiredTokenError,
    InvalidTokenError,
    TokenNotFoundError,
}
