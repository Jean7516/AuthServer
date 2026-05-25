package com.company.usermanagement.application.dto.command;

/** Comando para autenticar un usuario con email y contraseña. */
public record LoginCommand(
    String email,
    String password,
    String ipAddress,  // para registro en sesión y auditoría
    String userAgent
) {}
