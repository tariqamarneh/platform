package com.businessagent.auth.dto.response;

import com.businessagent.auth.model.enums.BusinessStatus;
import com.businessagent.auth.model.enums.Plan;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminBusinessResponse(
        UUID id,
        String name,
        Plan plan,
        BusinessStatus status,
        long userCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
