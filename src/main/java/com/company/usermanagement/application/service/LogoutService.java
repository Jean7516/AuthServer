package com.company.usermanagement.application.service;

import com.company.usermanagement.application.port.in.LogoutUseCase;
import com.company.usermanagement.application.port.out.AuditPort;
import com.company.usermanagement.application.port.out.TokenRepositoryPort;
import com.company.usermanagement.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

    private final TokenRepositoryPort tokenRepository;
    private final AuditPort           audit;

    @Override
    @Transactional
    public void execute(UserId userId, String refreshToken) {
        // Revocar TODOS los tokens del usuario (logout de todos los dispositivos)
        tokenRepository.revokeAllUserTokens(userId);

        audit.log(userId, null, "user.logout", "user", userId.toString(),
            Map.of("scope", "all_devices"));

        log.info("Logout completado para userId: {}", userId);
    }
}
