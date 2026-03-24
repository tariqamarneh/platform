package com.businessagent.channel.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record SendMessageRequest(
    @NotBlank String channelId,
    @NotBlank String to,
    @NotBlank String type,
    Map<String, Object> content
) {}
