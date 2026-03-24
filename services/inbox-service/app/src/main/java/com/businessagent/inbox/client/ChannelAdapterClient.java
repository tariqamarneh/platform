package com.businessagent.inbox.client;

import com.businessagent.inbox.config.AppProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChannelAdapterClient {

    private static final Logger log = LoggerFactory.getLogger(ChannelAdapterClient.class);
    private final AppProperties appProperties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public void sendMessage(String apiKey, String channelId, String to, String type, Object content) {
        String url = appProperties.services().channelAdapterUrl() + "/api/v1/messages/send";

        Map<String, Object> body = Map.of(
            "channelId", channelId,
            "to", to,
            "type", type,
            "content", content
        );

        log.info("Sending message via channel-adapter: channelId={}, to={}, type={}", channelId, to, type);

        try {
            restClient.post()
                .uri(url)
                .header("X-API-Key", apiKey)
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(String.class);
            log.debug("Message sent successfully via channel-adapter");
        } catch (Exception e) {
            log.error("Failed to send message via channel-adapter: {}", e.getMessage());
            throw new RuntimeException("Failed to send message: " + e.getMessage(), e);
        }
    }

    public void sendTypingIndicator(String apiKey, String channelId, String to) {
        String url = appProperties.services().channelAdapterUrl() + "/api/v1/messages/typing";

        Map<String, Object> body = Map.of("channelId", channelId, "to", to);

        log.debug("Sending typing indicator: channelId={}, to={}", channelId, to);

        try {
            restClient.post()
                .uri(url)
                .header("X-API-Key", apiKey)
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(String.class);
        } catch (Exception e) {
            log.warn("Failed to send typing indicator: {}", e.getMessage());
            // Best effort — don't throw
        }
    }

    public void markAsRead(String apiKey, String channelId, String messageId) {
        String url = appProperties.services().channelAdapterUrl() + "/api/v1/messages/read";

        Map<String, Object> body = Map.of("channelId", channelId, "messageId", messageId);

        log.debug("Marking message as read: channelId={}, messageId={}", channelId, messageId);

        try {
            restClient.post()
                .uri(url)
                .header("X-API-Key", apiKey)
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(String.class);
        } catch (Exception e) {
            log.warn("Failed to mark as read: {}", e.getMessage());
        }
    }
}
