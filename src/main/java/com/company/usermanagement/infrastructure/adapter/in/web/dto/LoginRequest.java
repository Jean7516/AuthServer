package com.company.usermanagement.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** DTO de entrada para el endpoint POST /auth/login. */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank        String password
) {}