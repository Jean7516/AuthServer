package com.company.usermanagement.infrastructure.adapter.in.web.controller;

import com.company.usermanagement.application.dto.command.*;
import com.company.usermanagement.application.dto.response.*;
import com.company.usermanagement.application.port.in.*;
import com.company.usermanagement.infrastructure.adapter.in.web.dto.*;
import com.company.usermanagement.infrastructure.security.service.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "CRUD de usuarios y gestión de roles")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final GetUserUseCase        getUserUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final DeleteUserUseCase     deleteUserUseCase;
    private final AssignRoleUseCase     assignRoleUseCase;
    private final RevokeRoleUseCase     revokeRoleUseCase;
    private final ClearRolesUseCase     clearRolesUseCase;

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_users.read')")
    @Operation(summary = "Listar todos los usuarios (paginado)")
    public ApiResponse<PageResponse<UserResponse>> listUsers(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(getUserUseCase.findAll(page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_users.read')")
    @Operation(summary = "Obtener usuario por ID")
    public ApiResponse<UserResponse> getById(@PathVariable String id) {
        return ApiResponse.ok(getUserUseCase.findById(id));
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener el usuario autenticado actual")
    public ApiResponse<UserResponse> getMe() {
        String userId = SecurityUtils.getCurrentUserIdOrThrow().toString();
        return ApiResponse.ok(getUserUseCase.findById(userId));
    }

    @PatchMapping("/me/password")
    @Operation(summary = "Cambiar contraseña del usuario autenticado")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest req) {
        String userId = SecurityUtils.getCurrentUserIdOrThrow().toString();
        changePasswordUseCase.execute(
            new ChangePasswordCommand(userId, req.currentPassword(), req.newPassword())
        );
        return ResponseEntity.ok(ApiResponse.ok(
            "Contraseña actualizada. Por seguridad debes volver a iniciar sesión."));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_users.delete')")
    @Operation(summary = "Eliminar usuario (borrado lógico)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String id) {
        String actorId = SecurityUtils.getCurrentUserIdOrThrow().toString();
        deleteUserUseCase.execute(new DeleteUserCommand(id, actorId));
        return ResponseEntity.ok(ApiResponse.ok("Usuario eliminado correctamente"));
    }

    @PostMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('PERMISSION_roles.manage')")
    @Operation(summary = "Asignar un rol a un usuario")
    public ApiResponse<UserResponse> assignRole(
            @PathVariable String id,
            @Valid @RequestBody AssignRoleRequest req) {
        String actorId = SecurityUtils.getCurrentUserIdOrThrow().toString();
        return ApiResponse.ok("Rol asignado correctamente",
            assignRoleUseCase.execute(
                new AssignRoleCommand(id, req.roleName(), actorId, req.expiresAt())
            ));
    }

    @DeleteMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('PERMISSION_roles.manage')")
    @Operation(summary = "Revocar todos los roles efectivos de un usuario")
    public ApiResponse<UserResponse> clearRoles(@PathVariable String id) {
        String actorId = SecurityUtils.getCurrentUserIdOrThrow().toString();
        return ApiResponse.ok("Roles revocados correctamente",
            clearRolesUseCase.execute(id, actorId));
    }

    @DeleteMapping("/{id}/roles/{roleName}")
    @PreAuthorize("hasAuthority('PERMISSION_roles.manage')")
    @Operation(summary = "Revocar un rol específico de un usuario")
    public ApiResponse<UserResponse> revokeRole(
            @PathVariable String id,
            @PathVariable String roleName) {
        String actorId = SecurityUtils.getCurrentUserIdOrThrow().toString();
        return ApiResponse.ok("Rol revocado correctamente",
            revokeRoleUseCase.execute(new RevokeRoleCommand(id, roleName, actorId))
        );
    }
}
