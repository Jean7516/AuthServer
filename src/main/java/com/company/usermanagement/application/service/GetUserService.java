package com.company.usermanagement.application.service;

import com.company.usermanagement.application.dto.response.PageResponse;
import com.company.usermanagement.application.dto.response.UserResponse;
import com.company.usermanagement.application.port.in.GetUserUseCase;
import com.company.usermanagement.application.port.out.UserRepositoryPort;
import com.company.usermanagement.domain.exception.UserNotFoundException;
import com.company.usermanagement.domain.valueobject.Email;
import com.company.usermanagement.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetUserService implements GetUserUseCase {

    private final UserRepositoryPort userRepository;

    @Override
    @Transactional(readOnly = true)      // readOnly: optimización de Hibernate (no dirty check)
    public UserResponse findById(String userId) {
        return userRepository.findById(UserId.of(userId))
            .map(UserResponse::from)
            .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findByEmail(String email) {
        return userRepository.findByEmail(Email.of(email))
            .map(UserResponse::from)
            .orElseThrow(() -> new UserNotFoundException(email));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> findAll(int page, int size) {
        // La paginación real se implementa en el adapter JPA.
        // Aquí se delega con los parámetros crudos.
        // TODO: conectar con paginación real en UserJpaAdapter
        return PageResponse.of(List.of(), page, size, 0);
    }
}
