package com.company.usermanagement.infrastructure.adapter.in.web.controller;

import com.company.usermanagement.application.dto.command.*;
import com.company.usermanagement.application.dto.response.ApiResponse;
import com.company.usermanagement.application.dto.response.AuthResponse;
import com.company.usermanagement.application.dto.response.UserResponse;
import com.company.usermanagement.application.port.in.*;
import com.company.usermanagement.infrastructure.adapter.in.web.dto.*;
import com.company.usermanagement.infrastructure.security.service.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller de autenticación.
 *
 * <p>Rutas públicas (sin JWT):
 * <ul>
 *   <li>POST /auth/register — registro de nuevo usuario</li>
 *   <li>POST /auth/login    — obtener access + refresh tokens</li>
 *   <li>POST /auth/refresh  — rotar el par de tokens</li>
 * </ul>
 *
 * <p>Rutas protegidas:
 * <ul>
 *   <li>POST /auth/logout   — revocar tokens del usuario actual</li>
 * </ul>
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Registro, login, refresh y logout")
public class AuthController {

    private final CreateUserUseCase  createUserUseCase;
    private final LoginUseCase       loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase      logoutUseCase;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar nuevo usuario")
    public ApiResponse<UserResponse> register(
            @Valid @RequestBody RegisterRequest req) {

        UserResponse user = createUserUseCase.execute(new CreateUserCommand(
                req.email(), req.password(), req.username(), req.roleName()
        ));
        return ApiResponse.ok("Usuario registrado exitosamente", user);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest req,
            HttpServletRequest httpRequest) {

        AuthResponse auth = loginUseCase.execute(new LoginCommand(
                req.email(),
                req.password(),
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent")
        ));
        return ApiResponse.ok("Login exitoso", auth);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotar el par de tokens")
    public ApiResponse<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest req,
            HttpServletRequest httpRequest) {

        AuthResponse auth = refreshTokenUseCase.execute(new RefreshTokenCommand(
                req.refreshToken(),
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent")
        ));
        return ApiResponse.ok("Tokens renovados", auth);
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión (revoca todos los tokens del usuario)")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest req) {

        logoutUseCase.execute(SecurityUtils.getCurrentUserIdOrThrow(), req.refreshToken());
        return ResponseEntity.ok(ApiResponse.ok("Sesión cerrada correctamente"));
    }
}
