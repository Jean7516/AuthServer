package com.company.usermanagement.infrastructure.security.service;

import com.company.usermanagement.application.port.out.UserRepositoryPort;
import com.company.usermanagement.domain.model.User;
import com.company.usermanagement.domain.valueobject.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Adaptador entre Spring Security y el dominio.
 *
 * <p>Spring Security llama a este servicio durante la autenticación para
 * cargar el usuario. Lo convertimos a {@link UserDetails} que Spring entiende.
 *
 * <p>Las authorities se asignan como {@code PERMISSION_nombre_permiso}
 * para poder usar {@code @PreAuthorize("hasAuthority('PERMISSION_users.create')")}
 * en los controllers.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepositoryPort    userRepository;
    private final JwtService            jwtService;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(Email.of(email))
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        // Permisos desde la caché/BD vía JwtService
        Set<SimpleGrantedAuthority> authorities = jwtService
            .resolvePermissions(user.getId())
            .stream()
            .map(perm -> new SimpleGrantedAuthority("PERMISSION_" + perm))
            .collect(Collectors.toSet());

        return org.springframework.security.core.userdetails.User
            .withUsername(user.getEmail().value())
            .password(user.getPassword().value())
            .authorities(authorities)
            .accountExpired(false)
            .accountLocked(!user.isActive())
            .credentialsExpired(false)
            .disabled(!user.isActive())
            .build();
    }
}
