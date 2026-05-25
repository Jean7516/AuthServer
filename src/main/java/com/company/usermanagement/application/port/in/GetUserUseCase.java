package com.company.usermanagement.application.port.in;

import com.company.usermanagement.application.dto.response.PageResponse;
import com.company.usermanagement.application.dto.response.UserResponse;

/** Puerto de entrada para consultar usuarios (queries, sin efectos secundarios). */
public interface GetUserUseCase {

    /**
     * @throws com.company.usermanagement.domain.exception.UserNotFoundException si no existe
     */
    UserResponse findById(String userId);

    UserResponse findByEmail(String email);

    PageResponse<UserResponse> findAll(int page, int size);
}
