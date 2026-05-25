package com.company.usermanagement.domain.exception;

/** Se lanza al intentar autenticar o usar un usuario desactivado o eliminado. */
public class UserInactiveException extends DomainException {

    private static final String CODE = "USER_INACTIVE";

    public UserInactiveException(String email) {
        super(CODE, "La cuenta del usuario está inactiva: " + email);
    }
}
