package com.company.usermanagement.domain.model;

import com.company.usermanagement.domain.valueobject.RoleId;
import com.company.usermanagement.domain.valueobject.UserId;

import java.time.Instant;

/**
 * Representa la asignación de un Rol a un Usuario.
 *
 * <p>Es un Value Object que vive dentro del Aggregate {@link User}.
 * No tiene identidad propia; su unicidad la define la combinación
 * {@code userId + roleId} a nivel de persistencia.
 *
 * <p>Inmutable: cambiar el estado genera una nueva instancia ({@link #deactivate()}).
 *
 * @param roleId       ID del rol asignado
 * @param assignedBy   ID de quien lo asignó (null = sistema)
 * @param expiresAt    fecha de expiración (null = permanente)
 * @param active       si la asignación está vigente
 * @param createdAt    cuándo se realizó la asignación
 */
public record UserRole(
    RoleId  roleId,
    UserId  assignedBy,
    Instant expiresAt,
    boolean active,
    Instant createdAt
) {

    public UserRole {
        if (roleId == null) throw new IllegalArgumentException("roleId es obligatorio");
        if (createdAt == null) throw new IllegalArgumentException("createdAt es obligatorio");
    }

    /** ¿La asignación sigue siendo válida considerando la fecha de expiración? */
    public boolean isEffective() {
        return active && (expiresAt == null || Instant.now().isBefore(expiresAt));
    }

    /** Retorna una nueva instancia desactivada (inmutabilidad). */
    public UserRole deactivate() {
        return new UserRole(roleId, assignedBy, expiresAt, false, createdAt);
    }
}
