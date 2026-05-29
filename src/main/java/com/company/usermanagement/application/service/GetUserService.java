package com.company.usermanagement.application.service;

import com.company.usermanagement.application.dto.response.PageResponse;
import com.company.usermanagement.application.dto.response.UserResponse;
import com.company.usermanagement.application.port.in.GetUserUseCase;
import com.company.usermanagement.application.port.out.UserRepositoryPort;
import com.company.usermanagement.domain.exception.UserNotFoundException;
import com.company.usermanagement.domain.model.User;
import com.company.usermanagement.domain.valueobject.Email;
import com.company.usermanagement.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetUserService implements GetUserUseCase {

    private final UserRepositoryPort userRepository;
    private final UserResponseMapper userResponseMapper;

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(String userId) {
        User user = userRepository.findById(UserId.of(userId))
            .orElseThrow(() -> new UserNotFoundException(userId));
        return userResponseMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findByEmail(String email) {
        User user = userRepository.findByEmail(Email.of(email))
            .orElseThrow(() -> new UserNotFoundException(email));
        return userResponseMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> findAll(int page, int size) {
        List<User> users = userRepository.findAll(page, size);
        long total       = userRepository.countAll();
        int  totalPages  = size == 0 ? 1 : (int) Math.ceil((double) total / size);

        List<UserResponse> content = users.stream()
            .map(userResponseMapper::toResponse)
            .collect(Collectors.toList());

        return new PageResponse<>(
            content,
            page,
            size,
            total,
            totalPages,
            page == 0,
            page >= totalPages - 1
        );
    }
}
