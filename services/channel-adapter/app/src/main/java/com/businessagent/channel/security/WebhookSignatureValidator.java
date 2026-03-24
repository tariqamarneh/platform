package com.businessagent.channel.security;

import com.businessagent.channel.config.AppProperties;
import com.businessagent.channel.exception.WebhookValidationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
@RequiredArgsConstructor
public class WebhookSignatureValidator {

    private static final Logger log = LoggerFactory.getLogger(WebhookSignatureValidator.class);
    private final AppProperties appProperties;

    public void validate(String signature, String body) {
        if (signature == null || !signature.startsWith("sha256=")) {
            throw new WebhookValidationException("Missing or invalid signature header");
        }

        String expectedSignature = computeSignature(body);
        String actualSignature = signature.substring(7); // Remove "sha256=" prefix

        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                actualSignature.getBytes(StandardCharsets.UTF_8))) {
            log.warn("Webhook signature mismatch");
            throw new WebhookValidationException("Invalid webhook signature");
        }
    }

    private String computeSignature(String body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    appProperties.meta().appSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new WebhookValidationException("Failed to compute signature", e);
        }
    }
}
