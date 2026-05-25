package com.company.usermanagement.application.dto.command;

/** Comando para rotar el refresh token y obtener un nuevo access token. */
public record RefreshTokenCommand(
    String refreshToken,
    String ipAddress,
    String userAgent
) {}
