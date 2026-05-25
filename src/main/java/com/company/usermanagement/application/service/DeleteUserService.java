package com.company.usermanagement.application.service;

import com.company.usermanagement.application.dto.command.DeleteUserCommand;
import com.company.usermanagement.application.port.in.DeleteUserUseCase;
import com.company.usermanagement.application.port.out.AuditPort;
import com.company.usermanagement.application.port.out.CachePort;
import com.company.usermanagement.application.port.out.TokenRepositoryPort;
import com.company.usermanagement.application.port.out.UserRepositoryPort;
import com.company.usermanagement.domain.exception.UserNotFoundException;
import com.company.usermanagement.domain.model.User;
import com.company.usermanagement.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteUserService implements DeleteUserUseCase {

    private final UserRepositoryPort  userRepository;
    private final TokenRepositoryPort tokenRepository;
    private final CachePort           cache;
    private final AuditPort           audit;

    @Override
    @Transactional
    public void execute(DeleteUserCommand command) {
        UserId targetId = UserId.of(command.targetUserId());
        UserId actorId  = UserId.of(command.requestedByUserId());

        User user = userRepository.findById(targetId)
            .orElseThrow(() -> new UserNotFoundException(command.targetUserId()));

        // El aggregate aplica la regla: no se puede eliminar a uno mismo en producción
        // (esa restricción se agrega aquí si aplica al negocio)
        user.delete(actorId);
        userRepository.save(user);

        tokenRepository.revokeAllUserTokens(targetId);
        cache.evictByPattern("*::" + command.targetUserId());

        audit.log(actorId, null, "user.deleted", "user", command.targetUserId(),
            Map.of("email", user.getEmail().value()));

        log.info("Usuario {} eliminado por actor {}", command.targetUserId(),
            command.requestedByUserId());
    }
}
