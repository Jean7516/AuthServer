package com.company.usermanagement.application.port.in;

import com.company.usermanagement.application.dto.command.LoginCommand;
import com.company.usermanagement.application.dto.response.AuthResponse;

/** Puerto de entrada para autenticación con email y contraseña. */
public interface LoginUseCase {

    /**
     * Autentica al usuario y devuelve los tokens de acceso.
     *
     * @throws com.company.usermanagement.domain.exception.InvalidCredentialsException si las credenciales son incorrectas
     * @throws com.company.usermanagement.domain.exception.UserInactiveException       si la cuenta está desactivada
     */
    AuthResponse execute(LoginCommand command);
}
