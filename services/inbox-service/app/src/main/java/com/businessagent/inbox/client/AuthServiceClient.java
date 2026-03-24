package com.businessagent.inbox.client;

import com.businessagent.inbox.config.AppProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceClient.class);
    private final AppProperties appProperties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public enum ValidationResult { VALID, INVALID, SERVICE_UNAVAILABLE }

    public record ApiKeyValidation(ValidationResult result, UUID businessId) {}

    /**
     * Validates an API key against auth-service.
     * Returns VALID with businessId, INVALID, or SERVICE_UNAVAILABLE.
     */
    public ApiKeyValidation validateApiKey(String apiKey) {
        String url = appProperties.services().authUrl() + "/api/v1/keys/verify";

        try {
            String responseBody = restClient.post()
                    .uri(url)
                    .header("X-API-Key", apiKey)
                    .header("Content-Type", "application/json")
                    .retrieve()
                    .body(String.class);

            JsonNode response = objectMapper.readTree(responseBody);
            boolean valid = response.get("valid").asBoolean();

            if (valid) {
                UUID businessId = UUID.fromString(response.get("businessId").asText());
                return new ApiKeyValidation(ValidationResult.VALID, businessId);
            }

            return new ApiKeyValidation(ValidationResult.INVALID, null);
        } catch (Exception e) {
            log.warn("Auth service unavailable: {}", e.getMessage());
            return new ApiKeyValidation(ValidationResult.SERVICE_UNAVAILABLE, null);
        }
    }
}
