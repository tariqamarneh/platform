package com.businessagent.channel.service;

import com.businessagent.channel.dto.webhook.InstagramWebhookPayload;
import com.businessagent.channel.dto.webhook.MetaWebhookPayload;

public interface WebhookService {
    String verifyWebhook(String token, String mode, String challenge, String verifyToken);
    void processWhatsAppWebhook(String webhookToken, MetaWebhookPayload payload);
    void processInstagramWebhook(String webhookToken, InstagramWebhookPayload payload);
}
