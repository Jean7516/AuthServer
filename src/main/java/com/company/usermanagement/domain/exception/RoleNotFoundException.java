package com.company.usermanagement.domain.exception;

/** Se lanza cuando no se encuentra un rol por nombre o ID. */
public class RoleNotFoundException extends DomainException {

    private static final String CODE = "ROLE_NOT_FOUND";

    public RoleNotFoundException(String identifier) {
        super(CODE, "Rol no encontrado: " + identifier);
    }
}
