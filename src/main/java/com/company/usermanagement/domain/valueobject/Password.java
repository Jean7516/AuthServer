package com.company.usermanagement.domain.valueobject;

/**
 * Value Object que representa una contraseña.
 *
 * <p>Existen dos estados posibles:
 * <ol>
 *   <li><b>Sin hashear</b>: recién ingresada por el usuario. Se valida la política
 *       de seguridad y nunca se persiste en este estado.</li>
 *   <li><b>Hasheada</b>: recuperada de la base de datos. No se puede validar la política
 *       porque el valor ya es opaco.</li>
 * </ol>
 *
 * <p>La distinción explícita entre ambos estados evita el bug clásico de persistir
 * una contraseña en texto plano porque se olvidó hashear antes de guardar.
 */
public record Password(String value, boolean isHashed) {

    // ─── Política de contraseñas ────────────────────────────
    private static final int    MIN_LENGTH      = 8;
    private static final int    MAX_LENGTH      = 128;
    private static final String UPPERCASE_RE    = ".*[A-Z].*";
    private static final String LOWERCASE_RE    = ".*[a-z].*";
    private static final String DIGIT_RE        = ".*\\d.*";
    private static final String SPECIAL_CHAR_RE = ".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*";

    public Password {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
    }

    // ─── Factory methods ────────────────────────────────────

    /**
     * Crea un Password desde el texto plano ingresado por el usuario.
     * Aplica la política de seguridad antes de aceptarlo.
     */
    public static Password ofPlainText(String rawPassword) {
        validate(rawPassword);
        return new Password(rawPassword, false);
    }

    /**
     * Reconstruye un Password desde el hash almacenado en BD.
     * No aplica validaciones de política (el valor ya es opaco).
     */
    public static Password ofHash(String hash) {
        if (hash == null || hash.isBlank()) {
            throw new IllegalArgumentException("El hash de contraseña no puede estar vacío");
        }
        return new Password(hash, true);
    }

    /** ¿Esta instancia contiene texto plano (aún no hasheado)? */
    public boolean isPlainText() {
        return !isHashed;
    }

    // ─── Validación de política ─────────────────────────────
    private static void validate(String raw) {
        if (raw.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                "La contraseña debe tener al menos " + MIN_LENGTH + " caracteres");
        }
        if (raw.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                "La contraseña no puede superar los " + MAX_LENGTH + " caracteres");
        }
        if (!raw.matches(UPPERCASE_RE)) {
            throw new IllegalArgumentException("La contraseña debe contener al menos una mayúscula");
        }
        if (!raw.matches(LOWERCASE_RE)) {
            throw new IllegalArgumentException("La contraseña debe contener al menos una minúscula");
        }
        if (!raw.matches(DIGIT_RE)) {
            throw new IllegalArgumentException("La contraseña debe contener al menos un número");
        }
        if (!raw.matches(SPECIAL_CHAR_RE)) {
            throw new IllegalArgumentException("La contraseña debe contener al menos un carácter especial");
        }
    }

    /**
     * Nunca exponer el hash en logs ni en toString.
     * Este método retorna siempre una representación oculta.
     */
    @Override
    public String toString() {
        return "[PROTECTED]";
    }
}
