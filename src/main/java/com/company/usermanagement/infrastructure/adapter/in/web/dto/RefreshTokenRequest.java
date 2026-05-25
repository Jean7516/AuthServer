package com.company.usermanagement.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "El refresh token es obligatorio") String refreshToken
) {}