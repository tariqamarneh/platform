package com.businessagent.auth.converter;

import com.businessagent.auth.dto.response.ApiKeyCreatedResponse;
import com.businessagent.auth.dto.response.ApiKeyResponse;
import com.businessagent.auth.model.ApiKey;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApiKeyConverterTest {

    private final ApiKeyConverter apiKeyConverter = new ApiKeyConverter();

    @Test
    void toResponse_shouldMapAllFieldsCorrectly() {
        UUID keyId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 15, 10, 30);
        ApiKey apiKey = buildApiKey(keyId, createdAt);

        ApiKeyResponse response = apiKeyConverter.toResponse(apiKey);

        assertNotNull(response);
        assertEquals(keyId, response.id());
        assertEquals("Production Key", response.name());
        assertEquals("ba_live_abcd", response.keyPrefix());
        assertEquals(createdAt, response.createdAt());
    }

    @Test
    void toCreatedResponse_shouldIncludeRawKey() {
        UUID keyId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 15, 10, 30);
        ApiKey apiKey = buildApiKey(keyId, createdAt);
        String rawKey = "ba_live_abcdef1234567890abcdef1234567890";

        ApiKeyCreatedResponse response = apiKeyConverter.toCreatedResponse(apiKey, rawKey);

        assertNotNull(response);
        assertEquals(keyId, response.id());
        assertEquals("Production Key", response.name());
        assertEquals("ba_live_abcd", response.keyPrefix());
        assertEquals(rawKey, response.rawKey());
        assertEquals(createdAt, response.createdAt());
    }

    private ApiKey buildApiKey(UUID keyId, LocalDateTime createdAt) {
        ApiKey apiKey = new ApiKey();
        apiKey.setId(keyId);
        apiKey.setBusinessId(UUID.randomUUID());
        apiKey.setName("Production Key");
        apiKey.setKeyHash("somehash");
        apiKey.setKeyPrefix("ba_live_abcd");
        apiKey.setCreatedAt(createdAt);
        return apiKey;
    }
}
