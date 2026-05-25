package com.company.usermanagement.infrastructure.security.service;

import com.company.usermanagement.application.port.out.CachePort;
import com.company.usermanagement.application.port.out.RoleRepositoryPort;
import com.company.usermanagement.application.port.out.UserRepositoryPort;
import com.company.usermanagement.domain.model.Permission;
import com.company.usermanagement.domain.model.User;
import com.company.usermanagement.domain.valueobject.UserId;
import com.company.usermanagement.infrastructure.config.AppProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio central de JWT.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Generar access tokens y refresh tokens firmados con HMAC-SHA256.</li>
 *   <li>Validar y parsear tokens.</li>
 *   <li>Resolver permisos efectivos de un usuario (con caché Redis).</li>
 *   <li>Hashear tokens antes de persistirlos (nunca se almacena texto plano).</li>
 * </ul>
 *
 * <p>Estructura del access token JWT:
 * <pre>
 * Header: { alg: HS256, typ: JWT }
 * Payload: {
 *   sub:         "uuid-del-usuario",
 *   email:       "user@example.com",
 *   permissions: ["users.read", "users.create", ...],
 *   type:        "access",
 *   iat:         1700000000,
 *   exp:         1700000900
 * }
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String CLAIM_EMAIL       = "email";
    private static final String CLAIM_PERMISSIONS = "permissions";
    private static final String CLAIM_TYPE        = "type";
    private static final String CLAIM_FAMILY      = "family";
    private static final String TYPE_ACCESS       = "access";
    private static final String TYPE_REFRESH      = "refresh";
    private static final String PERMISSIONS_CACHE_PREFIX = "permissions::";

    private final AppProperties      appProperties;
    private final UserRepositoryPort userRepository;
    private final RoleRepositoryPort roleRepository;
    private final CachePort          cache;

    // ═══════════════════════════════════════════════════════════
    //  Generación de tokens
    // ═══════════════════════════════════════════════════════════

    /**
     * Genera un access token JWT con los permisos del usuario incluidos como claim.
     * Duración: configurable en {@code app.jwt.access-token-expiry} (default 15m).
     */
    public String generateAccessToken(User user, Set<String> permissions) {
        Instant now    = Instant.now();
        Instant expiry = now.plus(appProperties.jwt().accessTokenExpiry());

        return Jwts.builder()
            .subject(user.getId().toString())
            .claim(CLAIM_EMAIL,       user.getEmail().value())
            .claim(CLAIM_PERMISSIONS, permissions)
            .claim(CLAIM_TYPE,        TYPE_ACCESS)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(getSigningKey())
            .compact();
    }

    /**
     * Genera un refresh token opaco (no contiene permisos).
     * Incluye un {@code family} UUID para detección de robo por reutilización.
     * Duración: configurable en {@code app.jwt.refresh-token-expiry} (default 7d).
     */
    public String generateRefreshToken(User user) {
        Instant now    = Instant.now();
        Instant expiry = now.plus(appProperties.jwt().refreshTokenExpiry());

        return Jwts.builder()
            .subject(user.getId().toString())
            .claim(CLAIM_TYPE,   TYPE_REFRESH)
            .claim(CLAIM_FAMILY, UUID.randomUUID().toString())   // nueva familia en cada login
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(getSigningKey())
            .compact();
    }

    // ═══════════════════════════════════════════════════════════
    //  Validación y extracción
    // ═══════════════════════════════════════════════════════════

    /**
     * Valida un access token. Retorna los claims si el token es válido.
     *
     * @throws JwtException si el token es inválido, expirado o manipulado
     */
    public Claims validateAccessToken(String token) {
        Claims claims = parseClaims(token);
        if (!TYPE_ACCESS.equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new JwtException("El token no es un access token");
        }
        return claims;
    }

    public String extractUserId(Claims claims) {
        return claims.getSubject();
    }

    public String extractEmail(Claims claims) {
        return claims.get(CLAIM_EMAIL, String.class);
    }

    @SuppressWarnings("unchecked")
    public Set<String> extractPermissions(Claims claims) {
        List<String> perms = claims.get(CLAIM_PERMISSIONS, List.class);
        return perms != null ? new HashSet<>(perms) : Collections.emptySet();
    }

    public UUID extractFamily(String token) {
        Claims claims = parseClaims(token);
        String family = claims.get(CLAIM_FAMILY, String.class);
        return family != null ? UUID.fromString(family) : UUID.randomUUID();
    }

    // ═══════════════════════════════════════════════════════════
    //  Permisos con caché Redis
    // ═══════════════════════════════════════════════════════════

    /**
     * Resuelve los permisos efectivos de un usuario.
     * Primer lookup en Redis; si no está, consulta BD y cachea el resultado.
     * TTL de caché: 5 minutos (balance entre consistencia y performance).
     */
    @SuppressWarnings("unchecked")
    public Set<String> resolvePermissions(UserId userId) {
        String cacheKey = PERMISSIONS_CACHE_PREFIX + userId.toString();

        return cache.get(cacheKey, Set.class).map(s -> (Set<String>) s)
            .orElseGet(() -> {
                Set<String> permissions = loadPermissionsFromDb(userId);
                cache.put(cacheKey, permissions, Duration.ofMinutes(5));
                return permissions;
            });
    }

    private Set<String> loadPermissionsFromDb(UserId userId) {
        return userRepository.findById(userId)
            .map(user -> user.getUserRoles().stream()
                .filter(ur -> ur.isEffective())
                .map(ur -> roleRepository.findById(ur.roleId()))
                .flatMap(Optional::stream)
                .filter(role -> role.isActive())
                .flatMap(role -> role.getPermissions().stream())
                .filter(Permission::isActive)
                .map(Permission::getName)
                .collect(Collectors.toSet())
            )
            .orElse(Collections.emptySet());
    }

    // ═══════════════════════════════════════════════════════════
    //  Utilidades
    // ═══════════════════════════════════════════════════════════

    /**
     * Genera el hash SHA-256 de un token para almacenamiento seguro en BD.
     * Nunca se persiste el token en texto plano.
     */
    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    public Instant getAccessTokenExpiry() {
        return Instant.now().plus(appProperties.jwt().accessTokenExpiry());
    }

    public Instant getRefreshTokenExpiry() {
        return Instant.now().plus(appProperties.jwt().refreshTokenExpiry());
    }

    // ═══════════════════════════════════════════════════════════
    //  Privado
    // ═══════════════════════════════════════════════════════════

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = appProperties.jwt().secret()
            .getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
