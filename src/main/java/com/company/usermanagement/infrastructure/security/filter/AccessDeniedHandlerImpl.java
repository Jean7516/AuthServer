package com.company.usermanagement.infrastructure.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/**
 * Maneja accesos de usuarios autenticados a recursos sin permiso (HTTP 403).
 *
 * <p>Diferencia clave con {@link AuthEntryPoint}:
 * <ul>
 *   <li>401 → no autenticado (no hay token o es inválido)</li>
 *   <li>403 → autenticado pero sin el permiso requerido</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        log.debug("Acceso denegado a: {} — {}", request.getRequestURI(), ex.getMessage());

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = Map.of(
            "success",   false,
            "message",   "No tienes permisos para realizar esta acción",
            "timestamp", Instant.now().toString()
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
