package com.company.usermanagement.domain.exception;

/**
 * Se lanza cuando las credenciales de login son incorrectas.
 *
 * <p>El mensaje es deliberadamente genérico para no indicar al atacante
 * si el email existe o si solo la contraseña es incorrecta.
 * El adapter lo mapea a HTTP 401.
 */
public class InvalidCredentialsException extends DomainException {

    private static final String CODE = "INVALID_CREDENTIALS";

    public InvalidCredentialsException() {
        super(CODE, "Email o contraseña incorrectos");
    }
}
