package com.businessagent.auth.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApiKeyCreatedResponse(
        UUID id,
        String name,
        String keyPrefix,
        String rawKey,
        LocalDateTime createdAt
) {
}
