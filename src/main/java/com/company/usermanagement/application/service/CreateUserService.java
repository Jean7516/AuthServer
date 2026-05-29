package com.company.usermanagement.application.service;

import com.company.usermanagement.application.dto.command.CreateUserCommand;
import com.company.usermanagement.application.dto.response.UserResponse;
import com.company.usermanagement.application.port.in.CreateUserUseCase;
import com.company.usermanagement.application.port.out.AuditPort;
import com.company.usermanagement.application.port.out.EmailNotificationPort;
import com.company.usermanagement.application.port.out.RoleRepositoryPort;
import com.company.usermanagement.application.port.out.UserRepositoryPort;
import com.company.usermanagement.domain.exception.RoleNotFoundException;
import com.company.usermanagement.domain.exception.UserAlreadyExistsException;
import com.company.usermanagement.domain.model.Role;
import com.company.usermanagement.domain.model.User;
import com.company.usermanagement.domain.service.PasswordHashingService;
import com.company.usermanagement.domain.valueobject.Email;
import com.company.usermanagement.domain.valueobject.Password;
import com.company.usermanagement.domain.valueobject.Username;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del caso de uso "Registrar usuario".
 *
 * <p>Orquesta el flujo completo sin contener lógica de negocio:
 * la lógica vive en el aggregate {@link User} y en los value objects.
 * Este service solo coordina colaboradores.
 *
 * <p>{@code @Transactional} garantiza que el save y el hasheo de contraseña
 * sean atómicos. Si el envío de email falla (que es @Async), no revierte la transacción.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateUserService implements CreateUserUseCase {

    private static final String DEFAULT_ROLE = "viewer";

    private final UserRepositoryPort     userRepository;
    private final RoleRepositoryPort     roleRepository;
    private final PasswordHashingService passwordHashingService;
    private final EmailNotificationPort  emailNotification;
    private final AuditPort              audit;
    private final UserResponseMapper     userResponseMapper;

    @Override
    @Transactional
    public UserResponse execute(CreateUserCommand command) {
        log.debug("Iniciando registro de usuario: {}", command.email());

        // 1. Construir value objects — la validación de formato ocurre aquí
        Email    email    = Email.of(command.email());
        Password password = Password.ofPlainText(command.password());
        Username username = command.username() != null
            ? Username.of(command.username()) : null;

        // 2. Verificar unicidad (regla de negocio que requiere consultar la BD)
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("email", email.value());
        }
        if (username != null && userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("username", username.value());
        }

        // 3. Crear el aggregate (emite UserCreatedEvent internamente)
        User user = User.create(email, password, username);

        // 4. Hashear contraseña usando el domain service
        user.hashPassword(passwordHashingService);

        // 5. Resolver el rol a asignar
        String roleName = command.roleName() != null ? command.roleName() : DEFAULT_ROLE;
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new RoleNotFoundException(roleName));
        user.assignRole(role, null, null);

        // 6. Persistir
        User saved = userRepository.save(user);

        // 7. Efectos secundarios asincrónicos (no revierten la transacción)
        emailNotification.sendVerificationEmail(
            saved.getEmail().value(),
            generateVerificationToken(saved)
        );

        audit.log(
            null, "system",
            "user.created", "user", saved.getId().toString(),
            java.util.Map.of("email", saved.getEmail().value(), "role", roleName)
        );

        log.info("Usuario creado exitosamente: {} con rol '{}'", saved.getId(), roleName);
        return userResponseMapper.toResponse(saved);
    }

    private String generateVerificationToken(User user) {
        // El token real se genera y persiste en EmailVerificationTokenAdapter
        // Aquí se delega; este método es un placeholder hasta implementar ese adapter
        return user.getId().toString();
    }
}
