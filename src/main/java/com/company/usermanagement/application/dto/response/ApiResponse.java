package com.company.usermanagement.application.dto.response;

import java.time.Instant;

/**
 * Envoltorio estándar para todas las respuestas de la API.
 *
 * <p>Formato consistente en toda la API:
 * <pre>
 * {
 *   "success": true,
 *   "message": "Usuario creado",
 *   "data": { ... },
 *   "timestamp": "2024-01-01T00:00:00Z"
 * }
 * </pre>
 *
 * @param <T> tipo del payload de datos
 */
public record ApiResponse<T>(
    boolean success,
    String  message,
    T       data,
    Instant timestamp
) {
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, Instant.now());
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", data, Instant.now());
    }

    public static ApiResponse<Void> ok(String message) {
        return new ApiResponse<>(true, message, null, Instant.now());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, Instant.now());
    }
}
