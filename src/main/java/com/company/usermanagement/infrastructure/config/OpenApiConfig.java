package com.company.usermanagement.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura Springdoc OpenAPI con autenticación JWT en Swagger UI.
 * Agrega el botón "Authorize" en la UI para enviar el Bearer token.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Management API")
                        .version("1.0.0")
                        .description("API base para gestión de usuarios, roles y permisos. Arquitectura Hexagonal."))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Ingresa el access token JWT obtenido en /auth/login")));
    }
}