package com.company.usermanagement.infrastructure.security.service;

import com.company.usermanagement.domain.exception.PermissionDeniedException;
import com.company.usermanagement.domain.valueobject.UserId;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * Utilidades estáticas para acceder al usuario autenticado en cualquier capa.
 *
 * <p>Uso en controllers:
 * <pre>
 *   UserId currentUser = SecurityUtils.getCurrentUserIdOrThrow();
 * </pre>
 *
 * <p>No se inyecta como bean para poder usarse en métodos estáticos desde cualquier
 * punto sin necesidad de inyección de dependencia.
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Retorna el UserId del usuario autenticado, o vacío si no hay sesión activa.
     */
    public static Optional<UserId> getCurrentUserId() {
        return Optional.ofNullable(getCurrentRequest())
            .map(req -> (String) req.getAttribute("userId"))
            .map(UserId::of);
    }

    /**
     * Retorna el UserId del usuario autenticado.
     * Lanza {@link PermissionDeniedException} si no hay usuario en el contexto.
     */
    public static UserId getCurrentUserIdOrThrow() {
        return getCurrentUserId()
            .orElseThrow(() -> new PermissionDeniedException("No hay sesión activa"));
    }

    /**
     * Retorna el email del usuario autenticado desde el principal de Spring Security.
     */
    public static Optional<String> getCurrentUserEmail() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
            .map(Authentication::getName);
    }

    /**
     * Verifica si el usuario autenticado tiene un permiso específico.
     *
     * <p>Para decisiones simples en código; para endpoints usar {@code @PreAuthorize}.
     */
    public static boolean hasPermission(String permission) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("PERMISSION_" + permission));
    }

    private static HttpServletRequest getCurrentRequest() {
        try {
            return ((ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes()).getRequest();
        } catch (IllegalStateException e) {
            return null;
        }
    }
}
