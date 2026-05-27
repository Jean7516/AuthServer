package com.company.usermanagement.infrastructure.adapter.in.web.controller;

import com.company.usermanagement.application.dto.response.*;
import com.company.usermanagement.application.port.in.GetAuditLogsUseCase;
import com.company.usermanagement.infrastructure.adapter.out.audit.AuditJpaRepository;
import com.company.usermanagement.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
import com.company.usermanagement.infrastructure.adapter.out.persistence.entity.UserRoleEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
@Tag(name = "Auditoría", description = "Registros de auditoría y estadísticas del dashboard")
@SecurityRequirement(name = "bearerAuth")
public class AuditController {

    private final GetAuditLogsUseCase auditUseCase;
    private final AuditJpaRepository  auditRepo;
    private final UserJpaRepository   userRepo;

    /** Lista paginada de logs con filtro opcional por acción. */
    @GetMapping("/logs")
    @PreAuthorize("hasAuthority('PERMISSION_audit.read')")
    @Operation(summary = "Listar registros de auditoría")
    public ApiResponse<PageResponse<AuditLogResponse>> getLogs(
            @RequestParam(defaultValue = "0")   int    page,
            @RequestParam(defaultValue = "20")  int    size,
            @RequestParam(required = false)     String action) {
        return ApiResponse.ok(auditUseCase.findAll(page, size, action));
    }

    /** Últimos N registros para el widget de actividad reciente. */
    @GetMapping("/recent")
    @PreAuthorize("hasAuthority('PERMISSION_audit.read')")
    @Operation(summary = "Actividad reciente (últimos N registros)")
    public ApiResponse<List<AuditLogResponse>> getRecent(
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.ok(auditUseCase.findRecent(limit));
    }

    /**
     * Estadísticas para el Overview del dashboard:
     * - total usuarios
     * - activos hoy
     * - logins fallidos hoy
     * - tokens activos (proxy)
     * - logins por día (7 días)
     * - distribución de usuarios por rol
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('PERMISSION_audit.read')")
    @Operation(summary = "Estadísticas del dashboard")
    public ApiResponse<DashboardStatsResponse> getStats() {
        Instant today    = Instant.now().truncatedTo(ChronoUnit.DAYS);
        Instant week     = Instant.now().minus(7, ChronoUnit.DAYS);
        Instant last24h  = Instant.now().minus(24, ChronoUnit.HOURS);

        // 1. Total usuarios
        long totalUsers = userRepo.count();

        // 2. Activos hoy (usuarios que hicieron login hoy)
        long activeToday = auditRepo.countActionSince("user.login", today);

        // 3. Logins fallidos hoy
        long failedToday = auditRepo.countActionSince("user.login_failed", today);

        // 4. Tokens activos (proxy: logins exitosos últimas 24h)
        long activeTokens = auditRepo.countActiveTokens(last24h);

        // 5. Logins por día (últimos 7 días)
        List<Object[]> rows = auditRepo.countByDayNative("user.login", week);
        List<DashboardStatsResponse.DayCount> loginsByDay = new ArrayList<>();
        for (Object[] row : rows) {
            String day   = (String) row[0];
            long   count = ((Number) row[1]).longValue();
            loginsByDay.add(new DashboardStatsResponse.DayCount(day, count));
        }

        // 6. Distribución por rol
        Map<String, Long> byRole = new HashMap<>();
        userRepo.findAll().forEach(user ->
            user.getUserRoles().stream()
                .filter(UserRoleEntity::isActive)
                .forEach(ur -> {
                    String roleName = ur.getRole().getName();
                    byRole.merge(roleName, 1L, Long::sum);
                })
        );

        return ApiResponse.ok(new DashboardStatsResponse(
            totalUsers, activeToday, failedToday, activeTokens, loginsByDay, byRole
        ));
    }
}
