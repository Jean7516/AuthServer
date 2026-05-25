package com.company.usermanagement.application.port.in;

import com.company.usermanagement.application.dto.command.RefreshTokenCommand;
import com.company.usermanagement.application.dto.response.AuthResponse;

/** Puerto de entrada para rotar el refresh token y obtener un nuevo access token. */
public interface RefreshTokenUseCase {

    /**
     * Valida el refresh token, lo invalida y emite un nuevo par de tokens.
     * Si el token ya fue usado (posible robo), revoca toda la familia.
     *
     * @throws com.company.usermanagement.domain.exception.InvalidCredentialsException si el token es inválido o expiró
     */
    AuthResponse execute(RefreshTokenCommand command);
}
