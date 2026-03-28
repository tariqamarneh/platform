package com.businessagent.inbox.worker;

import com.businessagent.inbox.client.ChannelAdapterClient;
import com.businessagent.inbox.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OutboundMessageProcessor {

    private static final Logger log = LoggerFactory.getLogger(OutboundMessageProcessor.class);
    private final OutboundPersistence persistence;
    private final ChannelAdapterClient channelAdapterClient;
    private final AppProperties appProperties;

    public void processMessage(String channelId, String messageId) {
        OutboundPersistence.OutboundContext ctx = persistence.loadContext(messageId);
        if (ctx == null) return;

        try {
            String apiKey = appProperties.services().serviceApiKey();
            if (apiKey == null || apiKey.isBlank()) {
                log.error("service-api-key not configured, cannot send outbound messages");
                persistence.markFailed(messageId);
                return;
            }

            channelAdapterClient.sendMessage(apiKey, channelId, ctx.contactExternalId(),
                    ctx.contentType(), ctx.content());

            persistence.markSent(messageId);
            log.info("Outbound message sent: messageId={}, to={}", messageId, ctx.contactExternalId());
        } catch (Exception e) {
            log.error("Failed to send outbound message: messageId={}, error={}", messageId, e.getMessage());
            persistence.handleFailure(channelId, messageId);
        }
    }
}
