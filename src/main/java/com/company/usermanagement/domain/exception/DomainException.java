package com.company.usermanagement.domain.exception;

/**
 * Excepción base del dominio. Toda excepción de negocio hereda de esta clase.
 *
 * <p>Convenciones:
 * <ul>
 *   <li>Son unchecked (heredan de {@link RuntimeException}).</li>
 *   <li>El {@code errorCode} es un código semántico legible (ej: {@code USER_NOT_FOUND})
 *       que la capa de infraestructura mapea al HTTP status correspondiente.</li>
 *   <li>El dominio nunca conoce HTTP, solo emite excepciones de negocio.</li>
 * </ul>
 */
public abstract class DomainException extends RuntimeException {

    private final String errorCode;

    protected DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected DomainException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
