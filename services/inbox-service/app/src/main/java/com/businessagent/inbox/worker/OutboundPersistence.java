package com.businessagent.inbox.worker;

import com.businessagent.inbox.config.AppProperties;
import com.businessagent.inbox.model.Contact;
import com.businessagent.inbox.model.Conversation;
import com.businessagent.inbox.model.Message;
import com.businessagent.inbox.model.enums.MessageStatus;
import com.businessagent.inbox.repository.ContactRepository;
import com.businessagent.inbox.repository.ConversationRepository;
import com.businessagent.inbox.repository.MessageRepository;
import com.businessagent.inbox.service.OutboundQueueService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboundPersistence {

    private static final Logger log = LoggerFactory.getLogger(OutboundPersistence.class);
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ContactRepository contactRepository;
    private final OutboundQueueService queueService;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    public record OutboundContext(String contactExternalId, String contentType, Object content) {}

    @Transactional(readOnly = true)
    public OutboundContext loadContext(String messageId) {
        Message message = messageRepository.findById(UUID.fromString(messageId)).orElse(null);
        if (message == null) { log.warn("Message not found: {}", messageId); return null; }

        Conversation conversation = conversationRepository.findById(message.getConversationId()).orElse(null);
        if (conversation == null) { log.warn("Conversation not found: {}", message.getConversationId()); return null; }

        Contact contact = contactRepository.findById(conversation.getContactId()).orElse(null);
        if (contact == null) { log.warn("Contact not found: {}", conversation.getContactId()); return null; }

        Object content;
        try { content = objectMapper.readValue(message.getContent(), Object.class); }
        catch (Exception e) { content = message.getContent(); }

        return new OutboundContext(contact.getExternalId(), message.getContentType().name().toLowerCase(), content);
    }

    @Transactional
    public void markSent(String messageId) {
        messageRepository.findById(UUID.fromString(messageId)).ifPresent(msg -> {
            msg.setStatus(MessageStatus.SENT);
            messageRepository.save(msg);
        });
    }

    @Transactional
    public void markFailed(String messageId) {
        messageRepository.findById(UUID.fromString(messageId)).ifPresent(msg -> {
            msg.setStatus(MessageStatus.FAILED);
            messageRepository.save(msg);
        });
    }

    @Transactional
    public void handleFailure(String channelId, String messageId) {
        messageRepository.findById(UUID.fromString(messageId)).ifPresent(msg -> {
            if (msg.getRetryCount() < appProperties.outbound().maxRetries()) {
                msg.setRetryCount(msg.getRetryCount() + 1);
                messageRepository.save(msg);
                queueService.requeue(channelId, messageId);
                log.info("Re-queued: messageId={}, retry={}", messageId, msg.getRetryCount());
            } else {
                msg.setStatus(MessageStatus.FAILED);
                messageRepository.save(msg);
                log.warn("Permanently failed after {} retries: messageId={}", appProperties.outbound().maxRetries(), messageId);
            }
        });
    }
}
