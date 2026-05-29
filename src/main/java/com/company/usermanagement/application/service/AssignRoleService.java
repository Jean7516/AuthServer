package com.company.usermanagement.application.service;

import com.company.usermanagement.application.dto.command.AssignRoleCommand;
import com.company.usermanagement.application.dto.command.RevokeRoleCommand;
import com.company.usermanagement.application.dto.response.UserResponse;
import com.company.usermanagement.application.port.in.AssignRoleUseCase;
import com.company.usermanagement.application.port.in.ClearRolesUseCase;
import com.company.usermanagement.application.port.in.RevokeRoleUseCase;
import com.company.usermanagement.application.port.out.AuditPort;
import com.company.usermanagement.application.port.out.CachePort;
import com.company.usermanagement.application.port.out.RoleRepositoryPort;
import com.company.usermanagement.application.port.out.UserRepositoryPort;
import com.company.usermanagement.domain.exception.RoleNotFoundException;
import com.company.usermanagement.domain.exception.UserNotFoundException;
import com.company.usermanagement.domain.model.Role;
import com.company.usermanagement.domain.model.User;
import com.company.usermanagement.domain.model.UserRole;
import com.company.usermanagement.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignRoleService implements AssignRoleUseCase, RevokeRoleUseCase, ClearRolesUseCase {

    private final UserRepositoryPort  userRepository;
    private final RoleRepositoryPort  roleRepository;
    private final CachePort           cache;
    private final AuditPort           audit;
    private final UserResponseMapper  userResponseMapper;

    /**
     * Asigna un rol al usuario.
     *
     * Regla de negocio: un usuario solo tiene UN rol a la vez.
     * Si ya tiene un rol, se revoca el anterior y se asigna el nuevo.
     */
    @Override
    @Transactional
    public UserResponse execute(AssignRoleCommand command) {
        User   user       = loadUser(command.targetUserId());
        Role   newRole    = loadRole(command.roleName());
        UserId assignedBy = command.assignedByUserId() != null
            ? UserId.of(command.assignedByUserId()) : null;

        // Revocar todos los roles actuales (un usuario = un rol)
        List<UserRole> currentRoles = user.getUserRoles().stream()
            .filter(UserRole::isEffective)
            .toList();

        for (UserRole ur : currentRoles) {
            roleRepository.findById(ur.roleId()).ifPresent(user::revokeRole);
        }

        // Asignar el nuevo rol
        user.assignRole(newRole, assignedBy, command.expiresAt());
        User saved = userRepository.save(user);

        cache.evict("permissions::" + command.targetUserId());

        audit.log(assignedBy, null, "role.assigned", "user", command.targetUserId(),
            Map.of("newRole", command.roleName(),
                   "previousRoles", currentRoles.stream()
                       .map(ur -> ur.roleId().toString()).toList()));

        log.info("Rol '{}' asignado a userId: {} (roles anteriores revocados: {})",
            command.roleName(), command.targetUserId(), currentRoles.size());

        return userResponseMapper.toResponse(saved);
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

        return userResponseMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse execute(String targetUserId, String actorId) {
        User   user      = loadUser(targetUserId);
        UserId revokedBy = actorId != null ? UserId.of(actorId) : null;

        List<UserRole> currentRoles = user.getUserRoles().stream()
            .filter(UserRole::isEffective)
            .toList();

        for (UserRole ur : currentRoles) {
            roleRepository.findById(ur.roleId()).ifPresent(user::revokeRole);
        }

        User saved = userRepository.save(user);

        cache.evict("permissions::" + targetUserId);

        audit.log(revokedBy, null, "roles.cleared", "user", targetUserId,
            Map.of("previousRoles", currentRoles.stream()
                .map(ur -> ur.roleId().toString()).toList()));

        log.info("Roles revocados para userId: {} ({} roles eliminados)",
            targetUserId, currentRoles.size());

        return userResponseMapper.toResponse(saved);
    }

    // ─── Helpers ─────────────────────────────────────────────

    private User loadUser(String userId) {
        return userRepository.findById(UserId.of(userId))
            .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private Role loadRole(String roleName) {
        return roleRepository.findByName(roleName)
            .orElseThrow(() -> new RoleNotFoundException(roleName));
    }
}
