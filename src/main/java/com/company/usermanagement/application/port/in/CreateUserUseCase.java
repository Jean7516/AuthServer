package com.company.usermanagement.application.port.in;

import com.company.usermanagement.application.dto.command.CreateUserCommand;
import com.company.usermanagement.application.dto.response.UserResponse;

/**
 * Puerto de entrada para el caso de uso de registro de usuarios.
 *
 * <p>Los puertos de entrada son las interfaces que expone la capa de aplicación
 * hacia afuera. El Controller (adapter IN) depende de esta interfaz,
 * nunca del service concreto. Esto permite sustituir la implementación
 * sin tocar la capa de infraestructura.
 */
public interface CreateUserUseCase {

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param command datos del nuevo usuario
     * @return representación pública del usuario creado
     * @throws com.company.usermanagement.domain.exception.UserAlreadyExistsException si el email ya existe
     */
    UserResponse execute(CreateUserCommand command);
}
