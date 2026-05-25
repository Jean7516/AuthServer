package com.company.usermanagement.application.service;

import com.company.usermanagement.application.dto.command.LoginCommand;
import com.company.usermanagement.application.dto.response.AuthResponse;
import com.company.usermanagement.application.dto.response.UserResponse;
import com.company.usermanagement.application.port.in.LoginUseCase;
import com.company.usermanagement.application.port.out.AuditPort;
import com.company.usermanagement.application.port.out.TokenRepositoryPort;
import com.company.usermanagement.application.port.out.UserRepositoryPort;
import com.company.usermanagement.domain.exception.InvalidCredentialsException;
import com.company.usermanagement.domain.exception.UserNotFoundException;
import com.company.usermanagement.domain.model.User;
import com.company.usermanagement.domain.service.PasswordHashingService;
import com.company.usermanagement.domain.valueobject.Email;
import com.company.usermanagement.infrastructure.security.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    private final UserRepositoryPort     userRepository;
    private final TokenRepositoryPort    tokenRepository;
    private final PasswordHashingService passwordHashingService;
    private final JwtService             jwtService;
    private final AuditPort              audit;

    @Override
    @Transactional
    public AuthResponse execute(LoginCommand command) {
        log.debug("Intento de login para: {}", command.email());

        // 1. Buscar usuario — error genérico para no revelar si el email existe
        User user = userRepository.findByEmail(Email.of(command.email()))
            .orElseThrow(InvalidCredentialsException::new);

        // 2. Verificar contraseña en el aggregate (regla de negocio del dominio)
        if (!user.matchesPassword(command.password(), passwordHashingService)) {
            audit.log(user.getId(), user.getEmail().value(),
                "user.login_failed", "user", user.getId().toString(),
                Map.of("ip", command.ipAddress(), "reason", "bad_password"));
            throw new InvalidCredentialsException();
        }

        // 3. Registrar login (actualiza lastLoginAt en el aggregate)
        user.recordLogin();
        userRepository.save(user);

        // 4. Obtener permisos efectivos para incluirlos en el JWT
        Set<String> permissions = jwtService.resolvePermissions(user.getId());

        // 5. Generar tokens
        String accessToken  = jwtService.generateAccessToken(user, permissions);
        String refreshToken = jwtService.generateRefreshToken(user);
        Instant expiresAt   = jwtService.getAccessTokenExpiry();

        // 6. Persistir refresh token (hasheado)
        tokenRepository.saveRefreshToken(
            user.getId(),
            jwtService.hashToken(refreshToken),
            jwtService.extractFamily(refreshToken),
            jwtService.getRefreshTokenExpiry()
        );

        audit.log(user.getId(), user.getEmail().value(),
            "user.login", "user", user.getId().toString(),
            Map.of("ip", command.ipAddress(), "agent", command.userAgent()));

        log.info("Login exitoso para userId: {}", user.getId());
        return AuthResponse.of(accessToken, refreshToken, expiresAt,
                               UserResponse.from(user), permissions);
    }
}
