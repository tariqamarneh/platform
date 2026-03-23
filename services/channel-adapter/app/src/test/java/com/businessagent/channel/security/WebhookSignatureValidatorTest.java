package com.businessagent.channel.security;

import com.businessagent.channel.config.AppProperties;
import com.businessagent.channel.exception.WebhookValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

class WebhookSignatureValidatorTest {

    private WebhookSignatureValidator validator;
    private static final String APP_SECRET = "test-secret";

    @BeforeEach
    void setUp() {
        AppProperties appProperties = new AppProperties(
                new AppProperties.MetaProperties(APP_SECRET, "test-verify-token", "v21.0", "https://graph.facebook.com"),
                new AppProperties.EncryptionProperties("test-encryption-key-32-bytes-ok!"),
                new AppProperties.ServicesProperties("http://localhost:8082", "http://localhost:8080")
        );
        validator = new WebhookSignatureValidator(appProperties);
    }

    @Test
    void validate_validSignature_shouldPass() throws Exception {
        String body = "{\"object\":\"whatsapp_business_account\"}";
        String hmac = computeHmac(body, APP_SECRET);
        String signature = "sha256=" + hmac;

        assertDoesNotThrow(() -> validator.validate(signature, body));
    }

    @Test
    void validate_invalidSignature_shouldThrowWebhookValidationException() {
        String body = "{\"object\":\"whatsapp_business_account\"}";
        String signature = "sha256=0000000000000000000000000000000000000000000000000000000000000000";

        assertThrows(WebhookValidationException.class,
                () -> validator.validate(signature, body));
    }

    @Test
    void validate_missingSignature_shouldThrowWebhookValidationException() {
        assertThrows(WebhookValidationException.class,
                () -> validator.validate("not-prefixed", "{\"body\":true}"));
    }

    @Test
    void validate_nullSignature_shouldThrowWebhookValidationException() {
        assertThrows(WebhookValidationException.class,
                () -> validator.validate(null, "{\"body\":true}"));
    }

    private String computeHmac(String body, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }
}
