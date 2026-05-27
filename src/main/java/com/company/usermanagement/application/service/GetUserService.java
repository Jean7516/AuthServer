package com.company.usermanagement.application.service;

import com.company.usermanagement.application.dto.response.PageResponse;
import com.company.usermanagement.application.dto.response.UserResponse;
import com.company.usermanagement.application.port.in.GetUserUseCase;
import com.company.usermanagement.application.port.out.RoleRepositoryPort;
import com.company.usermanagement.application.port.out.UserRepositoryPort;
import com.company.usermanagement.domain.exception.UserNotFoundException;
import com.company.usermanagement.domain.model.Role;
import com.company.usermanagement.domain.model.User;
import com.company.usermanagement.domain.valueobject.Email;
import com.company.usermanagement.domain.valueobject.RoleId;
import com.company.usermanagement.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetUserService implements GetUserUseCase {

    private final UserRepositoryPort userRepository;
    private final RoleRepositoryPort roleRepository;

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(String userId) {
        User user = userRepository.findById(UserId.of(userId))
            .orElseThrow(() -> new UserNotFoundException(userId));
        return toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findByEmail(String email) {
        User user = userRepository.findByEmail(Email.of(email))
            .orElseThrow(() -> new UserNotFoundException(email));
        return toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> findAll(int page, int size) {
        List<User> users = userRepository.findAll(page, size);
        long total       = userRepository.countAll();
        int  totalPages  = size == 0 ? 1 : (int) Math.ceil((double) total / size);

        List<UserResponse> content = users.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());

        return new PageResponse<>(
            content,
            page,
            size,
            total,
            totalPages,
            page == 0,
            page >= totalPages - 1
        );
    }

    /**
     * Convierte el aggregate User a UserResponse resolviendo los nombres
     * de roles a partir de sus IDs. Así el frontend recibe "admin", "viewer"
     * en lugar de UUIDs.
     */
    private UserResponse toResponse(User user) {
        Set<String> roleNames = user.getUserRoles().stream()
            .filter(ur -> ur.isEffective())
            .map(ur -> roleRepository.findById(RoleId.of(ur.roleId().value()))
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
