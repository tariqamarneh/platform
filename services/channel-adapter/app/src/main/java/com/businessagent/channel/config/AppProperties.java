package com.businessagent.channel.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
    MetaProperties meta,
    EncryptionProperties encryption,
    ServicesProperties services
) {
    public record MetaProperties(
        String appSecret,
        String verifyToken,
        String apiVersion,
        String apiBaseUrl
    ) {}

    public record EncryptionProperties(String key) {
        public EncryptionProperties {
            if (key == null || key.length() < 16) {
                throw new IllegalArgumentException("Encryption key must be at least 16 characters");
            }
        }
    }

    public record ServicesProperties(
        String inboxUrl,
        String authUrl
    ) {}
}
