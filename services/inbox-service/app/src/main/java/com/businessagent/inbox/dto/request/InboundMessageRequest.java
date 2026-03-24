package com.businessagent.inbox.dto.request;

import jakarta.validation.constraints.NotBlank;

public record InboundMessageRequest(
    @NotBlank String messageId,
    @NotBlank String channelId,
    @NotBlank String businessId,
    @NotBlank String from,
    String customerName,
    long timestamp,
    @NotBlank String type,
    Object payload
) {}
