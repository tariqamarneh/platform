package com.businessagent.channel.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TypingRequest(
    @NotBlank String channelId,
    @NotBlank String to
) {}
