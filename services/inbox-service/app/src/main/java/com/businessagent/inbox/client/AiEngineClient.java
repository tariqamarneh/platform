package com.businessagent.inbox.client;

import com.businessagent.inbox.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AiEngineClient {

    private static final Logger log = LoggerFactory.getLogger(AiEngineClient.class);
    private final AppProperties appProperties;
    private final RestClient restClient;

    public void notifyNewMessage(String conversationId, String businessId, String messageId,
                                  String contactId, String contactName, String message, String messageType) {
        String url = appProperties.services().aiEngineUrl() + "/api/v1/process";

        Map<String, Object> body = Map.of(
            "conversationId", conversationId,
            "businessId", businessId,
            "messageId", messageId,
            "contactId", contactId,
            "contactName", contactName != null ? contactName : "",
            "message", message,
            "messageType", messageType
        );

        log.info("Notifying ai-engine: conversationId={}, businessId={}", conversationId, businessId);

        try {
            restClient.post()
                .uri(url)
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .toBodilessEntity();
            log.debug("AI engine notified successfully");
        } catch (Exception e) {
            log.error("Failed to notify ai-engine: {}", e.getMessage());
            // Don't throw — AI engine being down shouldn't block message saving
        }
    }
}
