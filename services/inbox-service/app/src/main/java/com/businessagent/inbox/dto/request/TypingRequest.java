package com.businessagent.inbox.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TypingRequest(
    @NotBlank String conversationId
) {}
