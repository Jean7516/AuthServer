package com.company.usermanagement.infrastructure.security.service;

import com.company.usermanagement.domain.service.PasswordHashingService;
import com.company.usermanagement.infrastructure.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implementación del domain service {@link PasswordHashingService} usando BCrypt.
 *
 * <p>Vive en infraestructura porque depende de Spring Security.
 * El dominio solo conoce la interfaz. El strength (cost factor) se configura
 * en {@code app.security.bcrypt-strength} (default 12).
 *
 * <p>Cost factor 12 ≈ ~300ms por hash en hardware moderno.
 * Es el balance aceptado entre seguridad y latencia en login.
 */
@Service
@RequiredArgsConstructor
public class BcryptPasswordHashingService implements PasswordHashingService {

    private final AppProperties appProperties;

    // Lazy: se instancia una vez con el strength configurado
    private BCryptPasswordEncoder encoder;

    private BCryptPasswordEncoder getEncoder() {
        if (encoder == null) {
            encoder = new BCryptPasswordEncoder(
                appProperties.security().bcryptStrength()
            );
        }
        return encoder;
    }

    @Override
    public String hash(String rawPassword) {
        return getEncoder().encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String hash) {
        return getEncoder().matches(rawPassword, hash);
    }
}
