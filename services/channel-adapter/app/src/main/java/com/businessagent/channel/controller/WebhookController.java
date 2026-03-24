package com.businessagent.channel.controller;

import com.businessagent.channel.dto.webhook.InstagramWebhookPayload;
import com.businessagent.channel.dto.webhook.MetaWebhookPayload;
import com.businessagent.channel.security.WebhookSignatureValidator;
import com.businessagent.channel.service.WebhookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Channel webhook endpoints")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);
    private final WebhookService webhookService;
    private final WebhookSignatureValidator signatureValidator;
    private final ObjectMapper objectMapper;

    // --- Shared verification (same Meta pattern for both) ---

    @GetMapping("/webhook/whatsapp/{token}")
    @Operation(summary = "WhatsApp webhook verification")
    public ResponseEntity<String> verifyWhatsAppWebhook(
            @PathVariable String token,
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.challenge") String challenge,
            @RequestParam("hub.verify_token") String verifyToken) {
        return ResponseEntity.ok(webhookService.verifyWebhook(token, mode, challenge, verifyToken));
    }

    @GetMapping("/webhook/instagram/{token}")
    @Operation(summary = "Instagram webhook verification")
    public ResponseEntity<String> verifyInstagramWebhook(
            @PathVariable String token,
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.challenge") String challenge,
            @RequestParam("hub.verify_token") String verifyToken) {
        return ResponseEntity.ok(webhookService.verifyWebhook(token, mode, challenge, verifyToken));
    }

    // --- WhatsApp webhook ---

    @PostMapping("/webhook/whatsapp/{token}")
    @Operation(summary = "Receive WhatsApp webhook events")
    public ResponseEntity<Void> receiveWhatsAppWebhook(
            @PathVariable String token,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestBody String rawBody) {
        signatureValidator.validate(signature, rawBody);

        try {
            MetaWebhookPayload payload = objectMapper.readValue(rawBody, MetaWebhookPayload.class);
            webhookService.processWhatsAppWebhook(token, payload);
        } catch (Exception e) {
            log.error("Failed to process WhatsApp webhook: {}", e.getMessage());
        }

        return ResponseEntity.ok().build();
    }

    // --- Instagram webhook ---

    @PostMapping("/webhook/instagram/{token}")
    @Operation(summary = "Receive Instagram webhook events")
    public ResponseEntity<Void> receiveInstagramWebhook(
            @PathVariable String token,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestBody String rawBody) {
        signatureValidator.validate(signature, rawBody);

        try {
            InstagramWebhookPayload payload = objectMapper.readValue(rawBody, InstagramWebhookPayload.class);
            webhookService.processInstagramWebhook(token, payload);
        } catch (Exception e) {
            log.error("Failed to process Instagram webhook: {}", e.getMessage());
        }

        return ResponseEntity.ok().build();
    }
}
