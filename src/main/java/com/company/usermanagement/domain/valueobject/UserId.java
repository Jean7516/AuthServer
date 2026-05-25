package com.company.usermanagement.domain.valueobject;

import java.util.UUID;

/**
 * Value Object que representa el identificador único de un usuario.
 *
 * <p>Usar UUID tipado en lugar de UUID "crudo" evita errores de intercambio
 * accidental entre identificadores (pasar un RoleId donde se espera un UserId).
 */
public record UserId(UUID value) {

    public UserId {
        if (value == null) throw new IllegalArgumentException("UserId no puede ser nulo");
    }

    /** Genera un nuevo UserId aleatorio. */
    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }

    /** Reconstruye un UserId a partir de un String (persistencia, deserialización). */
    public static UserId of(String value) {
        try {
            return new UserId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("UserId inválido: " + value);
        }
    }

    public static UserId of(UUID value) {
        return new UserId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
