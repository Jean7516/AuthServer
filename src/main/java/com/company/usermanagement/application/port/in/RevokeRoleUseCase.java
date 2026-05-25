package com.company.usermanagement.application.port.in;

import com.company.usermanagement.application.dto.command.RevokeRoleCommand;
import com.company.usermanagement.application.dto.response.UserResponse;

/** Puerto de entrada para revocar un rol de un usuario. */
public interface RevokeRoleUseCase {
    UserResponse execute(RevokeRoleCommand command);
}
