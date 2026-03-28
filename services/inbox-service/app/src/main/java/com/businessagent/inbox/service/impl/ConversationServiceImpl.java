package com.businessagent.inbox.service.impl;

import com.businessagent.inbox.dto.response.ConversationResponse;
import com.businessagent.inbox.dto.response.MessageResponse;
import com.businessagent.inbox.exception.ConversationNotFoundException;
import com.businessagent.inbox.model.Conversation;
import com.businessagent.inbox.model.Message;
import com.businessagent.inbox.repository.ConversationRepository;
import com.businessagent.inbox.repository.MessageRepository;
import com.businessagent.inbox.service.ConversationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private static final Logger log = LoggerFactory.getLogger(ConversationServiceImpl.class);
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public ConversationResponse getConversation(UUID id) {
        Conversation conv = conversationRepository.findById(id)
            .orElseThrow(() -> new ConversationNotFoundException("Conversation not found: " + id));
        return toResponse(conv);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponse> getConversationMessages(UUID conversationId, Pageable pageable) {
        if (!conversationRepository.existsById(conversationId)) {
            throw new ConversationNotFoundException("Conversation not found: " + conversationId);
        }
        return messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable)
            .map(this::toMessageResponse);
    }

    private ConversationResponse toResponse(Conversation conv) {
        return new ConversationResponse(
            conv.getId(), conv.getBusinessId(), conv.getChannelId(), conv.getContactId(),
            conv.getStatus(), conv.getAssigneeType(), conv.getCreatedAt(), conv.getUpdatedAt());
    }

    private MessageResponse toMessageResponse(Message msg) {
        Object content;
        try {
            content = objectMapper.readValue(msg.getContent(), Object.class);
        } catch (Exception e) {
            content = msg.getContent();
        }
        return new MessageResponse(
            msg.getId(), msg.getConversationId(), msg.getDirection(), msg.getActorType(),
            msg.getContentType(), content, msg.getProviderMessageId(), msg.getStatus(), msg.getCreatedAt());
    }
}
