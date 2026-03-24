package com.businessagent.inbox.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReplyRequest(
    @NotBlank String conversationId,
    @NotBlank String businessId,
    @NotBlank String messageType,
    Object content
) {}
