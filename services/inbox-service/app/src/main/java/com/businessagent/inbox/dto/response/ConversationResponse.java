package com.businessagent.inbox.dto.response;

import com.businessagent.inbox.model.enums.AssigneeType;
import com.businessagent.inbox.model.enums.ConversationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConversationResponse(
    UUID id,
    UUID businessId,
    UUID channelId,
    UUID contactId,
    ConversationStatus status,
    AssigneeType assigneeType,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
