package com.company.usermanagement.application.service;

import com.company.usermanagement.application.dto.command.AssignRoleCommand;
import com.company.usermanagement.application.dto.command.RevokeRoleCommand;
import com.company.usermanagement.application.dto.response.UserResponse;
import com.company.usermanagement.application.port.in.AssignRoleUseCase;
import com.company.usermanagement.application.port.in.RevokeRoleUseCase;
import com.company.usermanagement.application.port.out.AuditPort;
import com.company.usermanagement.application.port.out.CachePort;
import com.company.usermanagement.application.port.out.RoleRepositoryPort;
import com.company.usermanagement.application.port.out.UserRepositoryPort;
import com.company.usermanagement.domain.exception.RoleNotFoundException;
import com.company.usermanagement.domain.exception.UserNotFoundException;
import com.company.usermanagement.domain.model.Role;
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
public class AssignRoleService implements AssignRoleUseCase, RevokeRoleUseCase {

    private final UserRepositoryPort userRepository;
    private final RoleRepositoryPort roleRepository;
    private final CachePort          cache;
    private final AuditPort          audit;

    @Override
    @Transactional
    public UserResponse execute(AssignRoleCommand command) {
        User   user       = loadUser(command.targetUserId());
        Role   role       = loadRole(command.roleName());
        UserId assignedBy = command.assignedByUserId() != null
            ? UserId.of(command.assignedByUserId()) : null;

        // La regla de negocio (no duplicados, usuario activo) está en el aggregate
        user.assignRole(role, assignedBy, command.expiresAt());
        User saved = userRepository.save(user);

        // Invalidar la caché de permisos del usuario para que el siguiente
        // request JWT lea los permisos actualizados
        cache.evict("permissions::" + command.targetUserId());

        audit.log(assignedBy, null, "role.assigned", "user", command.targetUserId(),
            Map.of("role", command.roleName(), "expiresAt", String.valueOf(command.expiresAt())));

        log.info("Rol '{}' asignado a userId: {}", command.roleName(), command.targetUserId());
        return UserResponse.from(saved);
    }

    @Override
    @Transactional
    public UserResponse execute(RevokeRoleCommand command) {
        User user = loadUser(command.targetUserId());
        Role role = loadRole(command.roleName());

        user.revokeRole(role);
        User saved = userRepository.save(user);

        cache.evict("permissions::" + command.targetUserId());

        audit.log(UserId.of(command.revokedByUserId()), null,
            "role.revoked", "user", command.targetUserId(),
            Map.of("role", command.roleName()));

        return UserResponse.from(saved);
    }

    private User loadUser(String userId) {
        return userRepository.findById(UserId.of(userId))
            .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private Role loadRole(String roleName) {
        return roleRepository.findByName(roleName)
            .orElseThrow(() -> new RoleNotFoundException(roleName));
    }
}
