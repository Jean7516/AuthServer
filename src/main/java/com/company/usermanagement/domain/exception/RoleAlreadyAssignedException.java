package com.company.usermanagement.domain.exception;

/** Se lanza al asignar un rol que el usuario ya posee. */
public class RoleAlreadyAssignedException extends DomainException {

    private static final String CODE = "ROLE_ALREADY_ASSIGNED";

    public RoleAlreadyAssignedException(String roleName) {
        super(CODE, "El usuario ya tiene asignado el rol: " + roleName);
    }
}
