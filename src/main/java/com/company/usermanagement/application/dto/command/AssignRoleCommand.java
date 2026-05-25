package com.company.usermanagement.application.dto.command;

import java.time.Instant;

/** Comando para asignar un rol a un usuario. */
public record AssignRoleCommand(
    String  targetUserId,
    String  roleName,
    String  assignedByUserId,
    Instant expiresAt          // null = permanente
) {}
