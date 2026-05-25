package com.company.usermanagement.domain.valueobject;

import java.util.regex.Pattern;

/**
 * Value Object que encapsula una dirección de email.
 *
 * <p>Reglas de negocio encapsuladas aquí:
 * <ul>
 *   <li>No puede ser nulo ni vacío.</li>
 *   <li>Debe tener formato válido de email.</li>
 *   <li>Se normaliza a minúsculas al crearse (equivalente a CITEXT en PostgreSQL).</li>
 *   <li>Longitud máxima: 255 caracteres.</li>
 * </ul>
 *
 * <p>Al ser un {@code record}, la igualdad estructural está garantizada por Java:
 * {@code new Email("A@B.com").equals(new Email("a@b.com"))} → {@code true}.
 */
public record Email(String value) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$"
    );
    private static final int MAX_LENGTH = 255;

    /**
     * Constructor compacto de record: se ejecuta al hacer {@code new Email("...")}.
     * Valida y normaliza el valor antes de asignarlo.
     */
    public Email {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
        value = value.trim().toLowerCase();    // normalización (re-asignación al parámetro)
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("El email supera los " + MAX_LENGTH + " caracteres");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Formato de email inválido: " + value);
        }
    }

    public static Email of(String value) {
        return new Email(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
