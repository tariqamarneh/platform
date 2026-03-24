package com.businessagent.inbox.client;

import com.businessagent.inbox.config.AppProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceClient.class);
    private final AppProperties appProperties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    /**
     * Validates an API key against auth-service.
     * Returns the business ID if valid, empty if invalid.
     */
    public Optional<UUID> validateApiKey(String apiKey) {
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
                return Optional.of(businessId);
            }

            return Optional.empty();
        } catch (Exception e) {
            log.warn("Failed to validate API key: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
