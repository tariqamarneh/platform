package com.businessagent.inbox.service;

import com.businessagent.inbox.dto.response.ConversationResponse;
import com.businessagent.inbox.dto.response.MessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ConversationService {
    ConversationResponse getConversation(UUID id);
    Page<MessageResponse> getConversationMessages(UUID conversationId, Pageable pageable);
}
