package com.businessagent.channel.service;

import com.businessagent.channel.dto.webhook.MetaWebhookPayload;

public interface WebhookService {
    String verifyWebhook(String token, String mode, String challenge, String verifyToken);
    void processWebhook(String webhookToken, MetaWebhookPayload payload);
}
