package com.company.usermanagement.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public record AssignRoleRequest(
        @NotBlank(message = "El nombre del rol es obligatorio") String roleName,
        Instant expiresAt   // opcional
) {}