package com.company.usermanagement.application.port.in;

import com.company.usermanagement.application.dto.response.AuditLogResponse;
import com.company.usermanagement.application.dto.response.PageResponse;

public interface GetAuditLogsUseCase {
    PageResponse<AuditLogResponse> findAll(int page, int size, String action);
    java.util.List<AuditLogResponse> findRecent(int limit);
}

