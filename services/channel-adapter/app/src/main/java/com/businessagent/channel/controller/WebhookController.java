package com.businessagent.channel.controller;

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
@Tag(name = "Webhooks", description = "Meta WhatsApp webhook endpoints")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);
    private final WebhookService webhookService;
    private final WebhookSignatureValidator signatureValidator;
    private final ObjectMapper objectMapper;

    @GetMapping("/webhook/{token}")
    @Operation(summary = "Meta webhook verification")
    public ResponseEntity<String> verifyWebhook(
            @PathVariable String token,
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.challenge") String challenge,
            @RequestParam("hub.verify_token") String verifyToken) {
        String result = webhookService.verifyWebhook(token, mode, challenge, verifyToken);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/webhook/{token}")
    @Operation(summary = "Receive Meta webhook events")
    public ResponseEntity<Void> receiveWebhook(
            @PathVariable String token,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestBody String rawBody) {
        // Validate signature
        signatureValidator.validate(signature, rawBody);

        // Parse and process
        try {
            MetaWebhookPayload payload = objectMapper.readValue(rawBody, MetaWebhookPayload.class);
            webhookService.processWebhook(token, payload);
        } catch (Exception e) {
            // Log but still return 200 to Meta to prevent retries
            log.error("Failed to process webhook: {}", e.getMessage());
        }

        return ResponseEntity.ok().build();
    }
}
