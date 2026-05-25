package com.company.usermanagement.application.port.in;

import com.company.usermanagement.domain.valueobject.UserId;

/** Puerto de entrada para cerrar sesión. */
public interface LogoutUseCase {

    /** Revoca el refresh token actual del usuario. */
    void execute(UserId userId, String refreshToken);
}
