package com.businessagent.inbox.service.impl;

import com.businessagent.inbox.dto.request.InboundMessageRequest;
import com.businessagent.inbox.model.Contact;
import com.businessagent.inbox.model.Conversation;
import com.businessagent.inbox.model.Message;
import com.businessagent.inbox.model.enums.*;
import com.businessagent.inbox.repository.ContactRepository;
import com.businessagent.inbox.repository.ConversationRepository;
import com.businessagent.inbox.repository.MessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InboundPersistenceService {

    private static final Logger log = LoggerFactory.getLogger(InboundPersistenceService.class);
    private final ContactRepository contactRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    @Getter
    public static class SaveResult {
        private final UUID conversationId;
        private final UUID messageId;
        private final UUID contactId;
        private final String contactName;

        public SaveResult(UUID conversationId, UUID messageId, UUID contactId, String contactName) {
            this.conversationId = conversationId;
            this.messageId = messageId;
            this.contactId = contactId;
            this.contactName = contactName;
        }
    }

    public SaveResult saveInbound(InboundMessageRequest request) {
        UUID businessId = UUID.fromString(request.businessId());
        UUID channelId = UUID.fromString(request.channelId());

        String channelProvider = request.channelProvider() != null && !request.channelProvider().isBlank()
            ? request.channelProvider() : "WHATSAPP";

        Contact contact = getOrCreateContact(businessId, request.from(), request.customerName(), channelProvider);
        Conversation conversation = getOrCreateConversation(businessId, channelId, contact.getId());
        Message message = saveInboundMessage(conversation.getId(), request);

        return new SaveResult(conversation.getId(), message.getId(), contact.getId(), contact.getDisplayName());
    }

    private Contact getOrCreateContact(UUID businessId, String externalId, String displayName, String provider) {
        // Try to find existing contact first
        var existing = contactRepository.findByBusinessIdAndExternalIdAndChannelProvider(businessId, externalId, provider);
        if (existing.isPresent()) return existing.get();

        // Try to create — if concurrent insert wins, re-fetch
        try {
            Contact contact = new Contact();
            contact.setBusinessId(businessId);
            contact.setExternalId(externalId);
            contact.setDisplayName(displayName);
            contact.setChannelProvider(provider);
            return contactRepository.saveAndFlush(contact);
        } catch (DataIntegrityViolationException e) {
            // Concurrent insert succeeded — re-fetch in a clean state
            return contactRepository.findByBusinessIdAndExternalIdAndChannelProvider(businessId, externalId, provider)
                .orElseThrow(() -> new RuntimeException("Failed to create or find contact"));
        }
    }

    private Conversation getOrCreateConversation(UUID businessId, UUID channelId, UUID contactId) {
        var existing = conversationRepository.findByContactIdAndChannelIdAndStatus(contactId, channelId, ConversationStatus.OPEN);
        if (existing.isPresent()) return existing.get();

        try {
            Conversation conversation = new Conversation();
            conversation.setBusinessId(businessId);
            conversation.setChannelId(channelId);
            conversation.setContactId(contactId);
            conversation.setStatus(ConversationStatus.OPEN);
            conversation.setAssigneeType(AssigneeType.AI_BOT);
            return conversationRepository.saveAndFlush(conversation);
        } catch (DataIntegrityViolationException e) {
            return conversationRepository.findByContactIdAndChannelIdAndStatus(contactId, channelId, ConversationStatus.OPEN)
                .orElseThrow(() -> new RuntimeException("Failed to create or find conversation"));
        }
    }

    private Message saveInboundMessage(UUID conversationId, InboundMessageRequest request) {
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setDirection(MessageDirection.INBOUND);
        message.setActorType(MessageActorType.CONTACT);
        message.setContentType(parseContentType(request.type()));
        message.setContent(serializePayload(request.payload()));
        message.setProviderMessageId(request.messageId());
        message.setStatus(MessageStatus.DELIVERED);
        return messageRepository.save(message);
    }

    private MessageContentType parseContentType(String type) {
        try {
            return MessageContentType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return MessageContentType.UNKNOWN;
        }
    }

    private String serializePayload(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            return "{}";
        }
    }
}
