package com.businessagent.inbox.worker;

import com.businessagent.inbox.client.ChannelAdapterClient;
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
public class OutboundMessageProcessor {

    private static final Logger log = LoggerFactory.getLogger(OutboundMessageProcessor.class);
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ContactRepository contactRepository;
    private final ChannelAdapterClient channelAdapterClient;
    private final OutboundQueueService queueService;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    @Transactional
    public void processMessage(String channelId, String messageId) {
        try {
            Message message = messageRepository.findById(UUID.fromString(messageId)).orElse(null);
            if (message == null) {
                log.warn("Message not found in DB: messageId={}", messageId);
                return;
            }

            Conversation conversation = conversationRepository.findById(message.getConversationId()).orElse(null);
            if (conversation == null) {
                log.warn("Conversation not found: conversationId={}", message.getConversationId());
                return;
            }

            Contact contact = contactRepository.findById(conversation.getContactId()).orElse(null);
            if (contact == null) {
                log.warn("Contact not found: contactId={}", conversation.getContactId());
                return;
            }

            Object content;
            try {
                content = objectMapper.readValue(message.getContent(), Object.class);
            } catch (Exception e) {
                content = message.getContent();
            }

            String apiKey = System.getenv("SERVICE_API_KEY");
            if (apiKey == null || apiKey.isBlank()) {
                log.error("SERVICE_API_KEY not configured, cannot send outbound messages");
                message.setStatus(MessageStatus.FAILED);
                messageRepository.save(message);
                return;
            }

            channelAdapterClient.sendMessage(
                apiKey,
                channelId,
                contact.getExternalId(),
                message.getContentType().name().toLowerCase(),
                content
            );

            message.setStatus(MessageStatus.SENT);
            messageRepository.save(message);
            log.info("Outbound message sent: messageId={}, to={}", messageId, contact.getExternalId());

        } catch (Exception e) {
            log.error("Failed to process outbound message: messageId={}, error={}", messageId, e.getMessage());
            handleFailure(channelId, messageId);
        }
    }

    private void handleFailure(String channelId, String messageId) {
        Message message = messageRepository.findById(UUID.fromString(messageId)).orElse(null);
        if (message == null) return;

        if (message.getRetryCount() < appProperties.outbound().maxRetries()) {
            message.setRetryCount(message.getRetryCount() + 1);
            messageRepository.save(message);
            queueService.requeue(channelId, messageId);
            log.info("Re-queued message for retry: messageId={}, retryCount={}", messageId, message.getRetryCount());
        } else {
            message.setStatus(MessageStatus.FAILED);
            messageRepository.save(message);
            log.warn("Message permanently failed after {} retries: messageId={}", appProperties.outbound().maxRetries(), messageId);
        }
    }
}
