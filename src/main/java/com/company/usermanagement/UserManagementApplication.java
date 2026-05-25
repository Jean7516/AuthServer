package com.company.usermanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Punto de entrada de la aplicación.
 *
 * <p>Anotaciones habilitadas desde aquí:
 * <ul>
 *   <li>{@code @EnableCaching}  — activa el soporte de caché con Redis</li>
 *   <li>{@code @EnableAsync}    — permite métodos asíncronos (@Async) para envío de emails y auditoría</li>
 *   <li>{@code @ConfigurationPropertiesScan} — detecta clases @ConfigurationProperties automáticamente</li>
 * </ul>
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@ConfigurationPropertiesScan
public class UserManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserManagementApplication.class, args);
    }
}
