package com.businessagent.channel.service.provider;

import com.businessagent.channel.config.AppProperties;
import com.businessagent.channel.dto.request.SendMessageRequest;
import com.businessagent.channel.dto.response.SendMessageResponse;
import com.businessagent.channel.exception.ProviderApiException;
import com.businessagent.channel.model.Channel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class InstagramProvider implements MessageProvider {

    private static final Logger log = LoggerFactory.getLogger(InstagramProvider.class);
    private final AppProperties appProperties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Override
    public SendMessageResponse sendMessage(Channel channel, String decryptedApiKey, SendMessageRequest request) {
        // Instagram Send API: POST /v21.0/{page-id}/messages
        String url = String.format("%s/%s/%s/messages",
                appProperties.meta().apiBaseUrl(), appProperties.meta().apiVersion(), channel.getPageId());

        Map<String, Object> body = new HashMap<>();
        body.put("recipient", Map.of("id", request.to()));

        // Build message based on type
        switch (request.type().toLowerCase()) {
            case "text" -> {
                @SuppressWarnings("unchecked")
                Map<String, Object> content = objectMapper.convertValue(request.content(), Map.class);
                body.put("message", Map.of("text", content.getOrDefault("body", content.getOrDefault("text", ""))));
            }
            case "image", "video", "audio" -> {
                // Instagram supports image, video, audio attachments (NOT file/document)
                @SuppressWarnings("unchecked")
                Map<String, Object> content = objectMapper.convertValue(request.content(), Map.class);
                String mediaUrl = (String) content.getOrDefault("url", content.getOrDefault("link", ""));
                body.put("message", Map.of(
                    "attachment", Map.of(
                        "type", request.type(),
                        "payload", Map.of("url", mediaUrl)
                    )
                ));
            }
            default -> {
                // Fallback: try to send as text
                body.put("message", Map.of("text", String.valueOf(request.content())));
            }
        }

        log.info("Sending Instagram message: pageId={}, to={}, type={}",
                channel.getPageId(), request.to(), request.type());

        try {
            String responseBody = restClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + decryptedApiKey)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode response = objectMapper.readTree(responseBody);
            String messageId = response.has("message_id") ? response.get("message_id").asText() : null;
            if (messageId != null) {
                log.info("Instagram message sent: messageId={}", messageId);
                return new SendMessageResponse(true, messageId, null);
            }

            // Alternative response format
            if (response.has("recipient_id")) {
                return new SendMessageResponse(true, response.get("recipient_id").asText(), null);
            }

            return new SendMessageResponse(false, null, "Unexpected response format");
        } catch (Exception e) {
            log.error("Failed to send Instagram message: {}", e.getMessage());
            throw new ProviderApiException(500, "Failed to send Instagram message: " + e.getMessage());
        }
    }

    @Override
    public void sendTypingIndicator(Channel channel, String decryptedApiKey, String to) {
        // Instagram Send API supports typing indicators via sender_action
        String url = String.format("%s/%s/%s/messages",
                appProperties.meta().apiBaseUrl(), appProperties.meta().apiVersion(), channel.getPageId());

        Map<String, Object> body = Map.of(
                "recipient", Map.of("id", to),
                "sender_action", "typing_on"
        );

        log.debug("Sending Instagram typing indicator: to={}", to);

        try {
            restClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + decryptedApiKey)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            log.warn("Failed to send Instagram typing indicator: {}", e.getMessage());
        }
    }

    @Override
    public void markAsRead(Channel channel, String decryptedApiKey, String messageId) {
        // Instagram doesn't support marking individual messages as read
        // This is a no-op — the sender_action "mark_seen" requires recipient ID
        // which is not available from just a messageId
        log.debug("Mark as read not supported for Instagram: messageId={}", messageId);
    }
}
