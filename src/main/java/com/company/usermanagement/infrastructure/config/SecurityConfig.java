package com.company.usermanagement.infrastructure.config;

import com.company.usermanagement.infrastructure.security.filter.AccessDeniedHandlerImpl;
import com.company.usermanagement.infrastructure.security.filter.AuthEntryPoint;
import com.company.usermanagement.infrastructure.security.filter.JwtAuthFilter;
import com.company.usermanagement.infrastructure.security.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuración central de Spring Security.
 *
 * <p>Decisiones de diseño:
 * <ul>
 *   <li><b>Stateless</b>: sin sesiones HTTP. Cada request se autentica con el JWT.</li>
 *   <li><b>CSRF deshabilitado</b>: no aplica a APIs REST stateless (sin cookies de sesión).</li>
 *   <li><b>@EnableMethodSecurity</b>: habilita {@code @PreAuthorize} en controllers y services
 *       para granularidad por endpoint y por permiso.</li>
 *   <li><b>JwtAuthFilter antes de UsernamePasswordAuthenticationFilter</b>: el filtro JWT
 *       corre primero y carga el contexto de seguridad; el filtro de form-login no aplica.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // habilita @PreAuthorize, @PostAuthorize, @Secured
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter          jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthEntryPoint         authEntryPoint;
    private final AccessDeniedHandlerImpl accessDeniedHandler;

    // ─── Rutas que no requieren autenticación ─────────────────
    private static final String[] PUBLIC_POST = {
        "/auth/login",
        "/auth/register",
        "/auth/refresh",
    };

    private static final String[] PUBLIC_GET = {
        "/actuator/health",
        "/actuator/info",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html"
    };

    // ═══════════════════════════════════════════════════════════
    //  Security Filter Chain — reglas de autorización HTTP
    // ═══════════════════════════════════════════════════════════
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ─── Deshabilitar CSRF (API REST stateless) ───────
            .csrf(AbstractHttpConfigurer::disable)

            // ─── CORS ─────────────────────────────────────────
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ─── Sin sesiones (cada request es independiente) ─
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ─── Reglas de autorización ───────────────────────
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, PUBLIC_POST).permitAll()
                .requestMatchers(HttpMethod.GET,  PUBLIC_GET).permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/logout").authenticated()
                // El resto requiere autenticación; la granularidad por permiso
                // se controla con @PreAuthorize en cada controller
                .anyRequest().authenticated()
            )

            // ─── Manejadores de error personalizados ──────────
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authEntryPoint)    // 401
                .accessDeniedHandler(accessDeniedHandler)    // 403
            )

            // ─── Proveedor de autenticación ───────────────────
            .authenticationProvider(authenticationProvider())

            // ─── Insertar filtro JWT antes del estándar ───────
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ═══════════════════════════════════════════════════════════
    //  Beans de autenticación
    // ═══════════════════════════════════════════════════════════

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Bean de PasswordEncoder expuesto para inyección en otros componentes si fuera necesario.
     * La lógica de hashing en el dominio usa {@link BcryptPasswordHashingService}
     * que envuelve este encoder con el strength configurado.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    // ═══════════════════════════════════════════════════════════
    //  CORS
    // ═══════════════════════════════════════════════════════════

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // En producción, restringir a los dominios del frontend
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
