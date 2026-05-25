package com.company.usermanagement.application.service;

import com.company.usermanagement.application.dto.command.ChangePasswordCommand;
import com.company.usermanagement.application.port.in.ChangePasswordUseCase;
import com.company.usermanagement.application.port.out.AuditPort;
import com.company.usermanagement.application.port.out.TokenRepositoryPort;
import com.company.usermanagement.application.port.out.UserRepositoryPort;
import com.company.usermanagement.domain.exception.InvalidCredentialsException;
import com.company.usermanagement.domain.exception.UserNotFoundException;
import com.company.usermanagement.domain.model.User;
import com.company.usermanagement.domain.service.PasswordHashingService;
import com.company.usermanagement.domain.valueobject.Password;
import com.company.usermanagement.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChangePasswordService implements ChangePasswordUseCase {

    private final UserRepositoryPort     userRepository;
    private final TokenRepositoryPort    tokenRepository;
    private final PasswordHashingService passwordHashingService;
    private final AuditPort              audit;

    @Override
    @Transactional
    public void execute(ChangePasswordCommand command) {
        User user = userRepository.findById(UserId.of(command.userId()))
            .orElseThrow(() -> new UserNotFoundException(command.userId()));

        // Verificar contraseña actual antes de permitir el cambio
        if (!user.matchesPassword(command.currentPassword(), passwordHashingService)) {
            throw new InvalidCredentialsException();
        }

        // El aggregate valida la política de la nueva contraseña
        user.changePassword(Password.ofPlainText(command.newPassword()));
        user.hashPassword(passwordHashingService);
        userRepository.save(user);

        // Seguridad: revocar todos los tokens activos tras cambio de contraseña
        // El usuario debe autenticarse de nuevo en todos sus dispositivos
        tokenRepository.revokeAllUserTokens(user.getId());

        audit.log(user.getId(), user.getEmail().value(),
            "user.password_changed", "user", user.getId().toString(),
            Map.of("tokens_revoked", true));

        log.info("Contraseña cambiada para userId: {}. Tokens revocados.", user.getId());
    }
}
