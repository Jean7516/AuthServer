package com.company.usermanagement.application.dto.command;

/** Comando para revocar un rol de un usuario. */
public record RevokeRoleCommand(
    String targetUserId,
    String roleName,
    String revokedByUserId
) {}
