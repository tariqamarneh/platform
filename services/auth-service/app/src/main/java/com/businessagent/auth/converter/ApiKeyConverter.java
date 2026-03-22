package com.businessagent.auth.converter;

import com.businessagent.auth.dto.response.ApiKeyCreatedResponse;
import com.businessagent.auth.dto.response.ApiKeyResponse;
import com.businessagent.auth.model.ApiKey;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyConverter {

    public ApiKeyResponse toResponse(ApiKey apiKey) {
        return new ApiKeyResponse(
                apiKey.getId(),
                apiKey.getName(),
                apiKey.getKeyPrefix(),
                apiKey.getCreatedAt()
        );
    }

    public ApiKeyCreatedResponse toCreatedResponse(ApiKey apiKey, String rawKey) {
        return new ApiKeyCreatedResponse(
                apiKey.getId(),
                apiKey.getName(),
                apiKey.getKeyPrefix(),
                rawKey,
                apiKey.getCreatedAt()
        );
    }
}
