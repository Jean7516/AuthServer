package com.company.usermanagement.domain.exception;

/**
 * Se lanza cuando no se encuentra un usuario por cualquier criterio
 * (ID, email, username). El adapter de infraestructura la mapea a HTTP 404.
 */
public class UserNotFoundException extends DomainException {

    private static final String CODE = "USER_NOT_FOUND";

    public UserNotFoundException(String identifier) {
        super(CODE, "Usuario no encontrado: " + identifier);
    }
}
