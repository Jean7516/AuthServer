package com.company.usermanagement.domain.valueobject;

import java.util.UUID;

/** Value Object que identifica un Rol de forma única. */
public record RoleId(UUID value) {

    public RoleId {
        if (value == null) throw new IllegalArgumentException("RoleId no puede ser nulo");
    }

    public static RoleId generate() { return new RoleId(UUID.randomUUID()); }
    public static RoleId of(String value) { return new RoleId(UUID.fromString(value)); }
    public static RoleId of(UUID value)   { return new RoleId(value); }

    @Override public String toString() { return value.toString(); }
}
