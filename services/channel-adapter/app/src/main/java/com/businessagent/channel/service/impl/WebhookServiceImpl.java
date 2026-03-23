package com.businessagent.channel.service.impl;

import com.businessagent.channel.client.InboxServiceClient;
import com.businessagent.channel.config.AppProperties;
import com.businessagent.channel.dto.internal.InboundMessage;
import com.businessagent.channel.dto.webhook.MetaWebhookPayload;
import com.businessagent.channel.exception.WebhookValidationException;
import com.businessagent.channel.model.Channel;
import com.businessagent.channel.repository.ChannelRepository;
import com.businessagent.channel.service.WebhookService;
import com.businessagent.channel.util.MessageNormalizer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WebhookServiceImpl implements WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookServiceImpl.class);
    private final AppProperties appProperties;
    private final ChannelRepository channelRepository;
    private final InboxServiceClient inboxServiceClient;

    @Override
    @Transactional(readOnly = true)
    public String verifyWebhook(String webhookToken, String mode, String challenge, String verifyToken) {
        // Verify the webhook token matches a registered channel
        Channel channel = channelRepository.findByWebhookToken(webhookToken)
                .orElseThrow(() -> new WebhookValidationException("Unknown webhook token"));

        if (!"subscribe".equals(mode)) {
            throw new WebhookValidationException("Invalid mode: " + mode);
        }

        if (!appProperties.meta().verifyToken().equals(verifyToken)) {
            throw new WebhookValidationException("Invalid verify token");
        }

        log.info("Webhook verified for channel: channelId={}", channel.getId());
        return challenge;
    }

    @Override
    @Transactional(readOnly = true)
    public void processWebhook(String webhookToken, MetaWebhookPayload payload) {
        Channel channel = channelRepository.findByWebhookToken(webhookToken)
                .orElseThrow(() -> new WebhookValidationException("Unknown webhook token"));

        if (payload.entry() == null) return;

        for (var entry : payload.entry()) {
            if (entry.changes() == null) continue;

            for (var change : entry.changes()) {
                if (change.value() == null || change.value().messages() == null) continue;

                for (var message : change.value().messages()) {
                    InboundMessage inbound = MessageNormalizer.normalize(channel, message, change.value());
                    inboxServiceClient.forwardMessage(inbound);
                }

                // Handle status updates (delivered, read, failed)
                if (change.value().statuses() != null) {
                    for (var status : change.value().statuses()) {
                        log.debug("Message status update: messageId={}, status={}", status.id(), status.status());
                        // TODO: Forward status updates to inbox-service when needed
                    }
                }
            }
        }
    }
}
