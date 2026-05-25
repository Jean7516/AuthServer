package com.company.usermanagement.domain.valueobject;

import java.util.UUID;

/** Value Object que identifica un Permiso de forma única. */
public record PermissionId(UUID value) {

    public PermissionId {
        if (value == null) throw new IllegalArgumentException("PermissionId no puede ser nulo");
    }

    public static PermissionId generate() { return new PermissionId(UUID.randomUUID()); }
    public static PermissionId of(UUID value) { return new PermissionId(value); }

    @Override public String toString() { return value.toString(); }
}
