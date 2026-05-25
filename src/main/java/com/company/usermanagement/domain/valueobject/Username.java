package com.company.usermanagement.domain.valueobject;

import java.util.regex.Pattern;

/**
 * Value Object para el nombre de usuario.
 * Reglas: 3-50 chars, solo letras, números, guión y guión bajo. Normalizado a minúsculas.
 */
public record Username(String value) {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-z0-9_\\-]{3,50}$");

    public Username {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El username no puede estar vacío");
        }
        value = value.trim().toLowerCase();
        if (!USERNAME_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                "Username inválido. Solo letras, números, _ y - (3-50 caracteres): " + value);
        }
    }

    public static Username of(String value) { return new Username(value); }

    @Override public String toString() { return value; }
}
