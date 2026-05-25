package com.company.usermanagement.domain.service;

/**
 * Interfaz de Domain Service para el hashing de contraseñas.
 *
 * <p>El dominio define el contrato (qué se necesita) pero no la implementación
 * (cómo se hace). La implementación concreta con BCrypt/Argon2 vive en
 * {@code infrastructure/security/service/BcryptPasswordHashingService.java}.
 *
 * <p>Esto aplica la Regla de Dependencia: el dominio no conoce ninguna
 * librería de infraestructura.
 */
public interface PasswordHashingService {

    /**
     * Genera el hash de una contraseña en texto plano.
     *
     * @param rawPassword contraseña sin hashear
     * @return hash criptográfico listo para persistir
     */
    String hash(String rawPassword);

    /**
     * Verifica si una contraseña en texto plano coincide con el hash almacenado.
     *
     * @param rawPassword contraseña ingresada por el usuario
     * @param hash        hash almacenado en la base de datos
     * @return {@code true} si la contraseña coincide
     */
    boolean matches(String rawPassword, String hash);
}
