package com.businessagent.inbox.dto.response;

public record ReplyResult(
    boolean success,
    String messageId,
    String error
) {}
