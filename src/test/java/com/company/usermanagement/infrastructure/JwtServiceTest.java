package com.company.usermanagement.infrastructure;

import com.company.usermanagement.application.port.out.CachePort;
import com.company.usermanagement.application.port.out.RoleRepositoryPort;
import com.company.usermanagement.application.port.out.UserRepositoryPort;
import com.company.usermanagement.domain.model.User;
import com.company.usermanagement.domain.service.PasswordHashingService;
import com.company.usermanagement.domain.valueobject.Email;
import com.company.usermanagement.domain.valueobject.Password;
import com.company.usermanagement.infrastructure.config.AppProperties;
import com.company.usermanagement.infrastructure.security.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService — Generación y validación de tokens")
class JwtServiceTest {

    @Mock AppProperties                  appProperties;
    @Mock AppProperties.JwtProperties    jwtProps;
    @Mock UserRepositoryPort             userRepository;
    @Mock RoleRepositoryPort             roleRepository;
    @Mock CachePort                      cache;

    JwtService jwtService;
    User       testUser;

    /** PasswordHashingService stub: hash fijo, matches siempre true. */
    private final PasswordHashingService stubHashing = new PasswordHashingService() {
        @Override public String  hash(String raw)              { return "$2a$12$stubHash"; }
        @Override public boolean matches(String raw, String h) { return true; }
    };

    @BeforeEach
    void setUp() {
        when(appProperties.jwt()).thenReturn(jwtProps);
        when(jwtProps.secret()).thenReturn("test-secret-key-that-is-long-enough-32chars!!");
        when(jwtProps.accessTokenExpiry()).thenReturn(Duration.ofMinutes(15));
        when(jwtProps.refreshTokenExpiry()).thenReturn(Duration.ofDays(7));

        // Caché vacía → fuerza resolución desde BD en resolvePermissions
        when(cache.get(anyString(), any())).thenReturn(Optional.empty());
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        jwtService = new JwtService(appProperties, userRepository, roleRepository, cache);

        // Crear usuario y hashear contraseña con el stub de una sola interfaz
        testUser = User.create(
                Email.of("test@ejemplo.com"),
                Password.ofPlainText("Secure@123!"),
                null
        );
        testUser.hashPassword(stubHashing);
    }

    @Test
    @DisplayName("Genera un access token con los claims correctos")
    void generates_access_token_with_claims() {
        Set<String> perms = Set.of("users.read", "users.create");
        String token = jwtService.generateAccessToken(testUser, perms);

        assertThat(token).isNotBlank();

        Claims claims = jwtService.validateAccessToken(token);
        assertThat(jwtService.extractUserId(claims)).isEqualTo(testUser.getId().toString());
        assertThat(jwtService.extractEmail(claims)).isEqualTo("test@ejemplo.com");
        assertThat(jwtService.extractPermissions(claims))
                .containsExactlyInAnyOrder("users.read", "users.create");
    }

    @Test
    @DisplayName("Rechaza un access token manipulado")
    void rejects_tampered_token() {
        String token = jwtService.generateAccessToken(testUser, Set.of()) + "TAMPERED";
        assertThatThrownBy(() -> jwtService.validateAccessToken(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Rechaza un refresh token presentado como access token")
    void rejects_refresh_token_as_access() {
        String refresh = jwtService.generateRefreshToken(testUser);
        assertThatThrownBy(() -> jwtService.validateAccessToken(refresh))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("access token");
    }

    @Test
    @DisplayName("hashToken es determinista para el mismo input")
    void hash_token_is_deterministic() {
        String token = "some-token-value";
        assertThat(jwtService.hashToken(token)).isEqualTo(jwtService.hashToken(token));
    }

    @Test
    @DisplayName("hashToken produce hashes distintos para inputs distintos")
    void hash_token_differs_per_input() {
        assertThat(jwtService.hashToken("token-a"))
                .isNotEqualTo(jwtService.hashToken("token-b"));
    }
}