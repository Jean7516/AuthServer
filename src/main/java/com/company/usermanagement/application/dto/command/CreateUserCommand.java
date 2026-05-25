package com.company.usermanagement.application.dto.command;

/**
 * Comando para registrar un nuevo usuario.
 *
 * <p>Los Commands representan la intención del usuario de cambiar el estado
 * del sistema. Son inmutables y contienen datos crudos (String), sin value objects
 * del dominio. La conversión ocurre dentro del use case.
 *
 * <p>La validación de formato (@NotNull, @Email) se aplica en el Controller
 * (capa de infraestructura) antes de llegar aquí.
 */
public record CreateUserCommand(
    String email,
    String password,
    String username,   // opcional
    String roleName    // opcional; si null, se asigna el rol "viewer" por defecto
) {}
