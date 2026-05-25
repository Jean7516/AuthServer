package com.company.usermanagement.infrastructure.security.filter;

import com.company.usermanagement.infrastructure.security.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Filtro JWT que se ejecuta una vez por request.
 *
 * <p>Flujo por request:
 * <ol>
 *   <li>Extrae el token del header {@code Authorization: Bearer <token>}.</li>
 *   <li>Valida la firma, expiración y tipo ("access").</li>
 *   <li>Extrae permisos directamente del payload del JWT (sin consultar la BD).</li>
 *   <li>Carga el {@link org.springframework.security.core.Authentication} en el {@code SecurityContext}.</li>
 *   <li>Ante cualquier error, deja el contexto vacío; Spring Security devolverá 401.</li>
 * </ol>
 *
 * <p>Los permisos vienen en el JWT para evitar una consulta a BD o Redis
 * en cada request. El TTL del access token (15m) limita el tiempo de validez
 * de permisos obsoletos. Al cambiar roles, se invalida la caché y los próximos
 * access tokens tendrán los permisos actualizados.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER  = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest  request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain         chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(AUTH_HEADER);

        // Si no hay header o no es Bearer, continuar sin autenticar
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            // 1. Validar firma y claims
            Claims claims = jwtService.validateAccessToken(token);

            // 2. Solo autenticar si no hay autenticación previa en el contexto
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                String      userId = jwtService.extractUserId(claims);
                String      email  = jwtService.extractEmail(claims);
                Set<String> perms  = jwtService.extractPermissions(claims);

                // 3. Construir authorities desde el claim del JWT (sin tocar BD)
                Set<SimpleGrantedAuthority> authorities = perms.stream()
                    .map(p -> new SimpleGrantedAuthority("PERMISSION_" + p))
                    .collect(Collectors.toSet());

                // 4. Crear autenticación. Principal = email para simplicidad;
                //    en el Controller se usa SecurityUtils.getCurrentUserId()
                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(email, null, authorities);

                // Adjuntar IP y user-agent al contexto de autenticación
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Agregar userId como detalle extra para uso en controllers
                request.setAttribute("userId", userId);

                SecurityContextHolder.getContext().setAuthentication(auth);
                log.debug("JWT válido para userId={}, permisos={}", userId, perms.size());
            }

        } catch (JwtException e) {
            // Token inválido, expirado o manipulado — limpiar contexto y continuar
            // Spring Security devolverá 401 si el endpoint lo requiere
            log.debug("JWT inválido: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }

    /**
     * No aplicar el filtro a rutas públicas para evitar procesamiento innecesario.
     * Spring Security ya las permite en la config, pero este skip ahorra parsear el header.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/auth/login")
            || path.startsWith("/auth/register")
            || path.startsWith("/auth/refresh")
            || path.startsWith("/actuator/health")
            || path.startsWith("/v3/api-docs")
            || path.startsWith("/swagger-ui");
    }
}
