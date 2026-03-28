package com.businessagent.inbox.service.impl;

import com.businessagent.inbox.client.ChannelAdapterClient;
import com.businessagent.inbox.config.AppProperties;
import com.businessagent.inbox.dto.request.ReplyRequest;
import com.businessagent.inbox.dto.request.TypingRequest;
import com.businessagent.inbox.dto.response.ReplyResult;
import com.businessagent.inbox.exception.ConversationNotFoundException;
import com.businessagent.inbox.model.Contact;
import com.businessagent.inbox.model.Conversation;
import com.businessagent.inbox.model.Message;
import com.businessagent.inbox.model.enums.MessageActorType;
import com.businessagent.inbox.model.enums.MessageContentType;
import com.businessagent.inbox.model.enums.MessageDirection;
import com.businessagent.inbox.model.enums.MessageStatus;
import com.businessagent.inbox.repository.ContactRepository;
import com.businessagent.inbox.repository.ConversationRepository;
import com.businessagent.inbox.repository.MessageRepository;
import com.businessagent.inbox.service.OutboundQueueService;
import com.businessagent.inbox.service.ReplyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReplyServiceImpl implements ReplyService {

    private static final Logger log = LoggerFactory.getLogger(ReplyServiceImpl.class);
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ContactRepository contactRepository;
    private final OutboundQueueService outboundQueueService;
    private final ChannelAdapterClient channelAdapterClient;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    @Override
    public ReplyResult sendReply(ReplyRequest request) {
        log.info("Processing reply: conversationId={}", request.conversationId());

        UUID conversationId = UUID.fromString(request.conversationId());
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new ConversationNotFoundException("Conversation not found"));

        // Save outbound message (auto-commits via Spring Data default transaction)
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setDirection(MessageDirection.OUTBOUND);
        message.setActorType(MessageActorType.AI_BOT);
        message.setContentType(parseContentType(request.messageType()));
        message.setContent(serializeContent(request.content()));
        message.setStatus(MessageStatus.PENDING);
        message = messageRepository.save(message);

        // Redis enqueue after DB commit
        outboundQueueService.enqueue(conversation.getChannelId().toString(), message.getId().toString());

        log.info("Reply enqueued: messageId={}, conversationId={}", message.getId(), conversationId);
        return new ReplyResult(true, message.getId().toString(), null);
    }

    @Override
    public void setTypingIndicator(TypingRequest request) {
        UUID conversationId = UUID.fromString(request.conversationId());
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new ConversationNotFoundException("Conversation not found"));
        Contact contact = contactRepository.findById(conversation.getContactId())
            .orElseThrow(() -> new ConversationNotFoundException("Contact not found"));

        String apiKey = appProperties.services().serviceApiKey();
        if (apiKey != null && !apiKey.isBlank()) {
            channelAdapterClient.sendTypingIndicator(apiKey, conversation.getChannelId().toString(), contact.getExternalId());
        } else {
            log.warn("service-api-key not configured, cannot send typing indicator");
        }
    }

    private MessageContentType parseContentType(String type) {
        try {
            return MessageContentType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return MessageContentType.TEXT;
        }
    }

    private String serializeContent(Object content) {
        try {
            return objectMapper.writeValueAsString(content);
        } catch (Exception e) {
            return "{}";
        }
    }
}
