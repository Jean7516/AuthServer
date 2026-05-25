package com.company.usermanagement.application.dto.command;

/** Comando para cambiar la contraseña de un usuario autenticado. */
public record ChangePasswordCommand(
    String userId,
    String currentPassword,
    String newPassword
) {}
