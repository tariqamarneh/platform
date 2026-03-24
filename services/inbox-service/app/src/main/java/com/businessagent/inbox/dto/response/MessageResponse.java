package com.businessagent.inbox.dto.response;

import com.businessagent.inbox.model.enums.MessageActorType;
import com.businessagent.inbox.model.enums.MessageContentType;
import com.businessagent.inbox.model.enums.MessageDirection;
import com.businessagent.inbox.model.enums.MessageStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageResponse(
    UUID id,
    UUID conversationId,
    MessageDirection direction,
    MessageActorType actorType,
    MessageContentType contentType,
    Object content,
    String providerMessageId,
    MessageStatus status,
    LocalDateTime createdAt
) {}
