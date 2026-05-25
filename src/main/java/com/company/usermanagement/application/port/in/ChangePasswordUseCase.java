package com.company.usermanagement.application.port.in;

import com.company.usermanagement.application.dto.command.ChangePasswordCommand;

/** Puerto de entrada para cambio de contraseña con la contraseña actual. */
public interface ChangePasswordUseCase {

    /**
     * @throws com.company.usermanagement.domain.exception.InvalidCredentialsException si currentPassword no coincide
     */
    void execute(ChangePasswordCommand command);
}
