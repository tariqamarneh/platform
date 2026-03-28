package com.businessagent.inbox.dto.response;

public record InboundResult(
    boolean success,
    String conversationId,
    String messageId,
    String error
) {}
