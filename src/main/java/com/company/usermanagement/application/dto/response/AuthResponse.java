package com.company.usermanagement.application.dto.response;

import java.time.Instant;
import java.util.Set;

/**
 * DTO de respuesta para operaciones de autenticación (login y refresh).
 *
 * <p>Contiene el access token JWT (de corta duración) y el refresh token
 * (de larga duración para rotar el access token sin re-autenticarse).
 */
public record AuthResponse(
    String      accessToken,
    String      refreshToken,
    String      tokenType,       // siempre "Bearer"
    Instant     expiresAt,       // cuándo expira el access token
    UserResponse user,
    Set<String> permissions      // permisos efectivos incluidos en el JWT claim
) {
    public static AuthResponse of(String accessToken, String refreshToken,
                                  Instant expiresAt, UserResponse user,
                                  Set<String> permissions) {
        return new AuthResponse(accessToken, refreshToken, "Bearer",
                                expiresAt, user, permissions);
    }
}
