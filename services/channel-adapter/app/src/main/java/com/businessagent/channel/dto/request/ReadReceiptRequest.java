package com.businessagent.channel.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReadReceiptRequest(
    @NotBlank String channelId,
    @NotBlank String messageId
) {}
