package com.company.usermanagement.infrastructure.adapter.in.web.controller;

import com.company.usermanagement.application.dto.response.ApiResponse;
import com.company.usermanagement.domain.exception.*;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones.
 *
 * <p>Traduce las excepciones del dominio y de infraestructura a respuestas
 * HTTP con el formato {@link ApiResponse} estándar. El dominio nunca
 * conoce los códigos HTTP; esa traducción ocurre aquí.
 *
 * <p>Tabla de mapeo:
 * <ul>
 *   <li>{@link UserNotFoundException}        → 404 Not Found</li>
 *   <li>{@link UserAlreadyExistsException}   → 409 Conflict</li>
 *   <li>{@link UserInactiveException}        → 403 Forbidden</li>
 *   <li>{@link InvalidCredentialsException}  → 401 Unauthorized</li>
 *   <li>{@link RoleNotFoundException}        → 404 Not Found</li>
 *   <li>{@link RoleAlreadyAssignedException} → 409 Conflict</li>
 *   <li>{@link PermissionDeniedException}    → 403 Forbidden</li>
 *   <li>{@link MethodArgumentNotValidException} → 400 Bad Request</li>
 *   <li>Cualquier otra                       → 500 Internal Server Error</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─── 404 Not Found ────────────────────────────────────────
    @ExceptionHandler({ UserNotFoundException.class, RoleNotFoundException.class })
    public ResponseEntity<ApiResponse<Void>> handleNotFound(DomainException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.getMessage()));
    }

    // ─── 409 Conflict ─────────────────────────────────────────
    @ExceptionHandler({ UserAlreadyExistsException.class, RoleAlreadyAssignedException.class })
    public ResponseEntity<ApiResponse<Void>> handleConflict(DomainException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.error(ex.getMessage()));
    }

    // ─── 401 Unauthorized ─────────────────────────────────────
    @ExceptionHandler({ InvalidCredentialsException.class,
                        BadCredentialsException.class,
                        JwtException.class })
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(Exception ex) {
        // Mensaje genérico para no dar pistas al atacante
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error("Credenciales inválidas o token expirado"));
    }

    // ─── 403 Forbidden ────────────────────────────────────────
    @ExceptionHandler({ PermissionDeniedException.class,
                        UserInactiveException.class,
                        AccessDeniedException.class })
    public ResponseEntity<ApiResponse<Void>> handleForbidden(Exception ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error(ex.getMessage()));
    }

    // ─── 400 Bad Request — errores de validación de @Valid ───
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                e -> e.getDefaultMessage() != null ? e.getDefaultMessage() : "Valor inválido",
                (e1, e2) -> e1   // si hay dos errores para el mismo campo, mantener el primero
            ));

        return ResponseEntity.badRequest()
            .body(new ApiResponse<>(false, "Error de validación", errors,
                java.time.Instant.now()));
    }

    // ─── 400 Bad Request — IllegalArgumentException (VO inválido) ─
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(ex.getMessage()));
    }

    // ─── 500 Internal Server Error ────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        log.error("Error inesperado: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Error interno del servidor. Por favor intenta más tarde."));
    }
}
