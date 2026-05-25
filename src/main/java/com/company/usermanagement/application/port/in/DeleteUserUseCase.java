package com.company.usermanagement.application.port.in;

import com.company.usermanagement.application.dto.command.DeleteUserCommand;

/** Puerto de entrada para borrado lógico de usuario. */
public interface DeleteUserUseCase {
    void execute(DeleteUserCommand command);
}
