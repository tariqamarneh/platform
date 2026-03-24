package com.businessagent.channel.dto.response;

public record SendMessageResponse(
    boolean success,
    String messageId,
    String error
) {}
