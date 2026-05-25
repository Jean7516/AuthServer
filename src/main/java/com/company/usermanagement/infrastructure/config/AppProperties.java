package com.company.usermanagement.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

/**
 * Mapea el bloque {@code app:} del application.yml a un bean fuertemente tipado.
 * Evita usar @Value dispersos por el código; todo en un solo lugar.
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
    JwtProperties     jwt,
    SecurityProperties security
) {
    public record JwtProperties(
        String   secret,
        Duration accessTokenExpiry,
        Duration refreshTokenExpiry
    ) {}

    public record SecurityProperties(
        int      bcryptStrength,
        int      maxLoginAttempts,
        Duration lockoutDuration
    ) {}
}
