package com.company.usermanagement.application.service;

import com.company.usermanagement.application.dto.response.UserResponse;
import com.company.usermanagement.application.port.out.RoleRepositoryPort;
import com.company.usermanagement.domain.model.Role;
import com.company.usermanagement.domain.model.User;
import com.company.usermanagement.domain.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserResponseMapper {

    private final RoleRepositoryPort roleRepository;

    public UserResponse toResponse(User user) {
        Set<String> roleNames = user.getUserRoles().stream()
            .filter(UserRole::isEffective)
            .map(ur -> roleRepository.findById(ur.roleId())
                .map(Role::getName)
                .orElse(ur.roleId().toString()))
            .collect(Collectors.toSet());

        return new UserResponse(
            user.getId().toString(),
            user.getEmail().value(),
            user.getUsername().map(u -> u.value()).orElse(null),
            user.isActive(),
            user.isVerified(),
            user.getLastLoginAt().orElse(null),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            roleNames
        );
    }
}
