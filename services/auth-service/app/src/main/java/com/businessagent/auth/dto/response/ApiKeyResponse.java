package com.businessagent.auth.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApiKeyResponse(
        UUID id,
        String name,
        String keyPrefix,
        LocalDateTime createdAt
) {
}
