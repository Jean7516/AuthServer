package com.company.usermanagement.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.*;

/** DTO de entrada para el endpoint POST /auth/register. */
public record RegisterRequest(

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        @Size(max = 255)
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres")
        String password,

        @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9_\\-]*$",
                message = "El username solo puede contener letras, números, _ y -")
        String username,

        String roleName
) {}