package com.company.usermanagement.infrastructure.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/**
 * Maneja los intentos de acceso sin autenticación (HTTP 401).
 *
 * <p>Sin esta clase, Spring retorna una página HTML de error.
 * Con ella, devolvemos JSON consistente con el formato {@code ApiResponse}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        log.debug("Acceso no autenticado a: {}", request.getRequestURI());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = Map.of(
            "success",   false,
            "message",   "Autenticación requerida",
            "timestamp", Instant.now().toString()
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
