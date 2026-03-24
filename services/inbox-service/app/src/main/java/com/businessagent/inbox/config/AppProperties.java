package com.businessagent.inbox.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
    ServicesProperties services,
    OutboundProperties outbound
) {
    public record ServicesProperties(
        String channelAdapterUrl,
        String aiEngineUrl,
        String authUrl,
        String serviceApiKey
    ) {}

    public record OutboundProperties(
        String queuePrefix,
        int pollIntervalMs,
        int batchSize,
        int maxRetries
    ) {}
}
