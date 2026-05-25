package com.company.usermanagement.application.dto.response;

import com.company.usermanagement.domain.model.User;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO de respuesta que representa los datos públicos de un usuario.
 *
 * <p>Nunca expone la contraseña ni el hash. La capa de aplicación
 * construye este DTO a partir del aggregate {@link User}; el dominio
 * no conoce este DTO.
 */
public record UserResponse(
    String  id,
    String  email,
    String  username,
    boolean active,
    boolean verified,
    Instant lastLoginAt,
    Instant createdAt,
    Instant updatedAt,
    Set<String> roles
) {
    /** Factory method: convierte el aggregate en el DTO de respuesta. */
    public static UserResponse from(User user) {
        Set<String> roleNames = user.getUserRoles().stream()
            .filter(ur -> ur.isEffective())
            .map(ur -> ur.roleId().value().toString())   // se sustituye por nombre en el mapper de infra
            .collect(Collectors.toSet());

        return new UserResponse(
            user.getId().toString(),
            user.getEmail().value(),
            user.getUsername().map(u -> u.value()).orElse(null),
            user.isActive(),
            user.isVerified(),
            user.getLastLoginAt().orElse(null),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            roleNames
        );
    }
}
