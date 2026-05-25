package com.company.usermanagement.application.port.in;

import com.company.usermanagement.application.dto.command.AssignRoleCommand;
import com.company.usermanagement.application.dto.response.UserResponse;

/** Puerto de entrada para asignar un rol a un usuario. */
public interface AssignRoleUseCase {

    /**
     * @throws com.company.usermanagement.domain.exception.UserNotFoundException      si el usuario no existe
     * @throws com.company.usermanagement.domain.exception.RoleNotFoundException      si el rol no existe
     * @throws com.company.usermanagement.domain.exception.RoleAlreadyAssignedException si ya lo tiene
     */
    UserResponse execute(AssignRoleCommand command);
}
