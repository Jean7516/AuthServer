package com.company.usermanagement.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
    UUID    id,
    UUID    actorId,
    String  actorEmail,
    String  action,
    String  resourceType,
    UUID    resourceId,
    String  newValues,
    Instant createdAt
) {}
