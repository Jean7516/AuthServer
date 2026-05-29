package com.company.usermanagement.application.dto.response;

import java.time.Instant;
import java.util.Set;

/**
 * DTO de respuesta que representa los datos públicos de un usuario.
 *
 * <p>Nunca expone la contraseña ni el hash.
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
}
