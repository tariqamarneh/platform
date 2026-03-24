package com.businessagent.channel.service.provider;

import com.businessagent.channel.config.AppProperties;
import com.businessagent.channel.dto.request.SendMessageRequest;
import com.businessagent.channel.dto.response.SendMessageResponse;
import com.businessagent.channel.exception.ProviderApiException;
import com.businessagent.channel.model.Channel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WhatsAppCloudProvider implements MessageProvider {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppCloudProvider.class);
    private final AppProperties appProperties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Override
    public SendMessageResponse sendMessage(Channel channel, String decryptedApiKey, SendMessageRequest request) {
        String url = buildUrl(channel.getPhoneNumberId(), "messages");

        ObjectNode body = objectMapper.createObjectNode();
        body.put("messaging_product", "whatsapp");
        body.put("to", request.to());
        body.put("type", request.type());

        // Add type-specific content
        if (request.content() != null) {
            body.set(request.type(), objectMapper.valueToTree(request.content()));
        }

        log.info("Sending WhatsApp message: phoneNumberId={}, to={}, type={}",
                channel.getPhoneNumberId(), request.to(), request.type());

        try {
            String responseBody = restClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + decryptedApiKey)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode response = objectMapper.readTree(responseBody);
            JsonNode messages = response.get("messages");
            if (messages != null && messages.isArray() && !messages.isEmpty()) {
                String messageId = messages.get(0).get("id").asText();
                log.info("Message sent successfully: messageId={}", messageId);
                return new SendMessageResponse(true, messageId, null);
            }

            return new SendMessageResponse(false, null, "No message ID in response");
        } catch (Exception e) {
            log.error("Failed to send WhatsApp message: {}", e.getMessage());
            throw new ProviderApiException(500, "Failed to send message: " + e.getMessage());
        }
    }

    @Override
    public void sendTypingIndicator(Channel channel, String decryptedApiKey, String to) {
        // Note: WhatsApp Cloud API doesn't have a direct "typing" endpoint.
        // The typing indicator is implicit when using the "read" status.
        // For a true typing indicator, we'd need to use a different approach.
        // For now, we mark as "read" which shows the blue check marks.
        log.debug("Sending typing indicator: to={}", to);
    }

    @Override
    public void markAsRead(Channel channel, String decryptedApiKey, String messageId) {
        String url = buildUrl(channel.getPhoneNumberId(), "messages");

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "status", "read",
                "message_id", messageId
        );

        log.debug("Marking message as read: messageId={}", messageId);

        try {
            restClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + decryptedApiKey)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            log.warn("Failed to mark message as read: {}", e.getMessage());
            // Don't throw — read receipts are best-effort
        }
    }

    private String buildUrl(String phoneNumberId, String endpoint) {
        return String.format("%s/%s/%s/%s",
                appProperties.meta().apiBaseUrl(),
                appProperties.meta().apiVersion(),
                phoneNumberId,
                endpoint);
    }
}
