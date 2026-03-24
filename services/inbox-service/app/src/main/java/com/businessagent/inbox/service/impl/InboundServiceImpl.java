package com.businessagent.inbox.service.impl;

import com.businessagent.inbox.client.AiEngineClient;
import com.businessagent.inbox.client.ChannelAdapterClient;
import com.businessagent.inbox.dto.request.InboundMessageRequest;
import com.businessagent.inbox.dto.response.InboundResult;
import com.businessagent.inbox.model.Contact;
import com.businessagent.inbox.model.Conversation;
import com.businessagent.inbox.model.Message;
import com.businessagent.inbox.model.enums.AssigneeType;
import com.businessagent.inbox.model.enums.ConversationStatus;
import com.businessagent.inbox.model.enums.MessageActorType;
import com.businessagent.inbox.model.enums.MessageContentType;
import com.businessagent.inbox.model.enums.MessageDirection;
import com.businessagent.inbox.model.enums.MessageStatus;
import com.businessagent.inbox.repository.ContactRepository;
import com.businessagent.inbox.repository.ConversationRepository;
import com.businessagent.inbox.repository.MessageRepository;
import com.businessagent.inbox.service.InboundService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class InboundServiceImpl implements InboundService {

    private static final Logger log = LoggerFactory.getLogger(InboundServiceImpl.class);
    private final ContactRepository contactRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AiEngineClient aiEngineClient;
    private final ChannelAdapterClient channelAdapterClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public InboundResult processInbound(InboundMessageRequest request, String apiKey) {
        log.info("Processing inbound message: messageId={}, businessId={}, from={}",
            request.messageId(), request.businessId(), request.from());

        try {
            UUID businessId = UUID.fromString(request.businessId());
            UUID channelId = UUID.fromString(request.channelId());

            // 1. Get or create contact
            Contact contact = getOrCreateContact(businessId, request.from(), request.customerName(), request.type());

            // 2. Get or create conversation
            Conversation conversation = getOrCreateConversation(businessId, channelId, contact.getId());

            // 3. Save inbound message
            Message message = saveInboundMessage(conversation.getId(), request);

            // 4. Mark as read + typing (fire and forget, async)
            CompletableFuture.runAsync(() -> {
                channelAdapterClient.markAsRead(apiKey, request.channelId(), request.messageId());
                channelAdapterClient.sendTypingIndicator(apiKey, request.channelId(), request.from());
            });

            // 5. Notify AI engine (async)
            String messageText = extractMessageText(request);
            CompletableFuture.runAsync(() ->
                aiEngineClient.notifyNewMessage(
                    conversation.getId().toString(),
                    request.businessId(),
                    message.getId().toString(),
                    contact.getId().toString(),
                    contact.getDisplayName(),
                    messageText,
                    request.type()
                )
            );

            log.info("Inbound message processed: conversationId={}, messageId={}",
                conversation.getId(), message.getId());

            return new InboundResult(true, conversation.getId().toString(), message.getId().toString(), null);
        } catch (Exception e) {
            log.error("Failed to process inbound message: {}", e.getMessage(), e);
            return new InboundResult(false, null, null, "Failed to process message");
        }
    }

    private Contact getOrCreateContact(UUID businessId, String externalId, String displayName, String type) {
        // Determine channel provider from message type context
        // For now, default to the external ID format
        String provider = "WHATSAPP"; // Will be passed by channel-adapter in the future

        return contactRepository.findByBusinessIdAndExternalIdAndChannelProvider(businessId, externalId, provider)
            .orElseGet(() -> {
                try {
                    Contact contact = new Contact();
                    contact.setBusinessId(businessId);
                    contact.setExternalId(externalId);
                    contact.setDisplayName(displayName);
                    contact.setChannelProvider(provider);
                    return contactRepository.save(contact);
                } catch (DataIntegrityViolationException e) {
                    // Concurrent insert — re-fetch
                    return contactRepository.findByBusinessIdAndExternalIdAndChannelProvider(businessId, externalId, provider)
                        .orElseThrow(() -> new RuntimeException("Failed to create or find contact"));
                }
            });
    }

    private Conversation getOrCreateConversation(UUID businessId, UUID channelId, UUID contactId) {
        return conversationRepository.findByContactIdAndChannelIdAndStatus(contactId, channelId, ConversationStatus.OPEN)
            .orElseGet(() -> {
                try {
                    Conversation conversation = new Conversation();
                    conversation.setBusinessId(businessId);
                    conversation.setChannelId(channelId);
                    conversation.setContactId(contactId);
                    conversation.setStatus(ConversationStatus.OPEN);
                    conversation.setAssigneeType(AssigneeType.AI_BOT);
                    return conversationRepository.save(conversation);
                } catch (DataIntegrityViolationException e) {
                    return conversationRepository.findByContactIdAndChannelIdAndStatus(contactId, channelId, ConversationStatus.OPEN)
                        .orElseThrow(() -> new RuntimeException("Failed to create or find conversation"));
                }
            });
    }

    private Message saveInboundMessage(UUID conversationId, InboundMessageRequest request) {
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setDirection(MessageDirection.INBOUND);
        message.setActorType(MessageActorType.CONTACT);
        message.setContentType(parseContentType(request.type()));
        message.setContent(serializePayload(request.payload()));
        message.setProviderMessageId(request.messageId());
        message.setStatus(MessageStatus.DELIVERED); // Inbound messages are already delivered
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

    private String extractMessageText(InboundMessageRequest request) {
        try {
            var node = objectMapper.valueToTree(request.payload());
            if (node.has("text")) return node.get("text").asText();
            if (node.has("body")) return node.get("body").asText();
            if (node.has("caption")) return node.get("caption").asText();
            return objectMapper.writeValueAsString(request.payload());
        } catch (Exception e) {
            return "";
        }
    }
}
