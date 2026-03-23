package com.businessagent.channel.client;

import com.businessagent.channel.config.AppProperties;
import com.businessagent.channel.dto.internal.InboundMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class InboxServiceClient {

    private static final Logger log = LoggerFactory.getLogger(InboxServiceClient.class);
    private final AppProperties appProperties;
    private final RestClient restClient;

    public void forwardMessage(InboundMessage message) {
        String url = appProperties.services().inboxUrl() + "/api/v1/inbound";

        log.info("Forwarding message to inbox-service: messageId={}, businessId={}",
                message.messageId(), message.businessId());

        try {
            restClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .body(message)
                    .retrieve()
                    .toBodilessEntity();

            log.debug("Message forwarded successfully: messageId={}", message.messageId());
        } catch (Exception e) {
            log.error("Failed to forward message to inbox-service: messageId={}, error={}",
                    message.messageId(), e.getMessage());
            // Don't throw — we don't want to fail the webhook response to Meta
            // Messages will be retried by Meta if we return non-200
        }
    }
}
