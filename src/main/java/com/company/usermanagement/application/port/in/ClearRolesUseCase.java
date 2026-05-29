package com.company.usermanagement.application.port.in;

import com.company.usermanagement.application.dto.response.UserResponse;

/** Puerto de entrada para revocar todos los roles efectivos de un usuario. */
public interface ClearRolesUseCase {
    UserResponse execute(String targetUserId, String actorId);
}
