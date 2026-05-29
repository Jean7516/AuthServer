package com.company.usermanagement.application.service;

import com.company.usermanagement.application.dto.command.RefreshTokenCommand;
import com.company.usermanagement.application.dto.response.AuthResponse;
import com.company.usermanagement.application.dto.response.UserResponse;
import com.company.usermanagement.application.port.in.RefreshTokenUseCase;
import com.company.usermanagement.application.port.out.TokenRepositoryPort;
import com.company.usermanagement.application.port.out.TokenRepositoryPort.RefreshTokenData;
import com.company.usermanagement.application.port.out.UserRepositoryPort;
import com.company.usermanagement.domain.exception.InvalidCredentialsException;
import com.company.usermanagement.domain.exception.UserNotFoundException;
import com.company.usermanagement.domain.model.User;
import com.company.usermanagement.infrastructure.security.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService implements RefreshTokenUseCase {

    private final TokenRepositoryPort tokenRepository;
    private final UserRepositoryPort  userRepository;
    private final JwtService          jwtService;
    private final UserResponseMapper  userResponseMapper;

    @Override
    @Transactional
    public AuthResponse execute(RefreshTokenCommand command) {
        String hash = jwtService.hashToken(command.refreshToken());

        // 1. Buscar el token por su hash
        RefreshTokenData stored = tokenRepository.findActiveRefreshToken(hash)
            .orElseThrow(InvalidCredentialsException::new);

        // 2. Si el token ya fue usado → posible robo → revocar toda la familia
        if (!stored.isValid()) {
            log.warn("Reutilización de refresh token detectada. Revocando familia: {}",
                stored.family());
            tokenRepository.revokeTokenFamily(stored.family());
            throw new InvalidCredentialsException();
        }

        // 3. Marcar como usado (rotación: este token ya no sirve)
        tokenRepository.markRefreshTokenAsUsed(hash);

        // 4. Cargar usuario
        User user = userRepository.findById(stored.userId())
            .orElseThrow(() -> new UserNotFoundException(stored.userId().toString()));

        // 5. Emitir nuevos tokens
        Set<String> permissions = jwtService.resolvePermissions(user.getId());
        String newAccessToken   = jwtService.generateAccessToken(user, permissions);
        String newRefreshToken  = jwtService.generateRefreshToken(user);

        // 6. Persistir el nuevo refresh token manteniendo la misma familia
        tokenRepository.saveRefreshToken(
            user.getId(),
            jwtService.hashToken(newRefreshToken),
            stored.family(),                          // ← misma familia para seguir el rastro
            jwtService.getRefreshTokenExpiry()
        );

        return AuthResponse.of(newAccessToken, newRefreshToken,
                               jwtService.getAccessTokenExpiry(),
                               userResponseMapper.toResponse(user), permissions);
    }
}
