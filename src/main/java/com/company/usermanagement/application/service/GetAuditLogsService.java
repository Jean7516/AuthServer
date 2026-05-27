package com.company.usermanagement.application.service;

import com.company.usermanagement.application.dto.response.AuditLogResponse;
import com.company.usermanagement.application.dto.response.PageResponse;
import com.company.usermanagement.application.port.in.GetAuditLogsUseCase;
import com.company.usermanagement.infrastructure.adapter.out.audit.AuditJpaRepository;
import com.company.usermanagement.infrastructure.adapter.out.audit.AuditLogEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetAuditLogsService implements GetAuditLogsUseCase {

    private final AuditJpaRepository auditRepo;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> findAll(int page, int size, String action) {
        String actionFilter = (action == null || action.isBlank()) ? null : action;
        Page<AuditLogResponse> result = auditRepo
            .findAllFiltered(actionFilter, PageRequest.of(page, size))
            .map(this::toResponse);

        return new PageResponse<>(
            result.getContent(),
            page, size,
            result.getTotalElements(),
            result.getTotalPages(),
            result.isFirst(),
            result.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> findRecent(int limit) {
        return auditRepo.findTop20ByOrderByCreatedAtDesc()
            .stream()
            .limit(limit)
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    private AuditLogResponse toResponse(AuditLogEntity e) {
        return new AuditLogResponse(
            e.getId(), e.getActorId(), e.getActorEmail(),
            e.getAction(), e.getResourceType(), e.getResourceId(),
            e.getNewValues(), e.getCreatedAt()
        );
    }
}
