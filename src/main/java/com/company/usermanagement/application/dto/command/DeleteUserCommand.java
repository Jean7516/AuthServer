package com.company.usermanagement.application.dto.command;

/** Comando para eliminar un usuario (borrado lógico). */
public record DeleteUserCommand(
    String targetUserId,
    String requestedByUserId
) {}
