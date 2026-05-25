package com.company.usermanagement.domain.exception;

/** Se lanza cuando el usuario no tiene permiso para ejecutar una acción. */
public class PermissionDeniedException extends DomainException {

    private static final String CODE = "PERMISSION_DENIED";

    public PermissionDeniedException(String permission) {
        super(CODE, "Acción no autorizada. Se requiere el permiso: " + permission);
    }
}
