package com.company.usermanagement.application.dto.response;

import com.company.usermanagement.domain.model.Permission;
import com.company.usermanagement.domain.model.Role;

import java.util.Set;
import java.util.stream.Collectors;

/** DTO de respuesta para un Rol con sus permisos. */
public record RoleResponse(
    String          id,
    String          name,
    String          displayName,
    String          description,
    boolean         system,
    boolean         active,
    Set<String>     permissions    // nombres de permisos activos: "users.create", etc.
) {
    public static RoleResponse from(Role role) {
        Set<String> permNames = role.getPermissions().stream()
            .filter(Permission::isActive)
            .map(Permission::getName)
            .collect(Collectors.toSet());

        return new RoleResponse(
            role.getId().toString(),
            role.getName(),
            role.getDisplayName(),
            role.getDescription(),
            role.isSystem(),
            role.isActive(),
            permNames
        );
    }
}
