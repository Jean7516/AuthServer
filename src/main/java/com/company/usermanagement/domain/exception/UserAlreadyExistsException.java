package com.company.usermanagement.domain.exception;

/**
 * Se lanza al intentar registrar un usuario con un email o username
 * que ya existe en el sistema. El adapter lo mapea a HTTP 409.
 */
public class UserAlreadyExistsException extends DomainException {

    private static final String CODE = "USER_ALREADY_EXISTS";

    public UserAlreadyExistsException(String field, String value) {
        super(CODE, "Ya existe un usuario con " + field + ": " + value);
    }
}
