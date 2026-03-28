package com.businessagent.inbox.service.impl;

import com.businessagent.inbox.client.AiEngineClient;
import com.businessagent.inbox.client.ChannelAdapterClient;
import com.businessagent.inbox.dto.request.InboundMessageRequest;
import com.businessagent.inbox.dto.response.InboundResult;
import com.businessagent.inbox.service.InboundService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class InboundServiceImpl implements InboundService {

    private static final Logger log = LoggerFactory.getLogger(InboundServiceImpl.class);
    private final InboundPersistenceService persistenceService;
    private final ChannelAdapterClient channelAdapterClient;
    private final AiEngineClient aiEngineClient;
    private final ObjectMapper objectMapper;

    private final ExecutorService asyncExecutor = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    public InboundResult processInbound(InboundMessageRequest request, String apiKey) {
        log.info("Processing inbound message: messageId={}, businessId={}, from={}",
            request.messageId(), request.businessId(), request.from());

        InboundPersistenceService.SaveResult dbResult;
        try {
            dbResult = persistenceService.saveInbound(request);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid inbound request: {}", e.getMessage());
            return new InboundResult(false, null, null, "Invalid request: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to save inbound message: {}", e.getMessage(), e);
            return new InboundResult(false, null, null, "Failed to process message");
        }

        // Async tasks AFTER transaction committed
        asyncExecutor.execute(() -> {
            try {
                channelAdapterClient.markAsRead(apiKey, request.channelId(), request.messageId());
                channelAdapterClient.sendTypingIndicator(apiKey, request.channelId(), request.from());
            } catch (Exception e) {
                log.warn("Failed to send read receipt/typing: {}", e.getMessage());
            }
        });

        asyncExecutor.execute(() -> {
            try {
                String messageText = extractMessageText(request);
                aiEngineClient.notifyNewMessage(
                    dbResult.getConversationId().toString(),
                    request.businessId(),
                    dbResult.getMessageId().toString(),
                    dbResult.getContactId().toString(),
                    dbResult.getContactName(),
                    messageText,
                    request.type()
                );
            } catch (Exception e) {
                log.warn("Failed to notify AI engine: {}", e.getMessage());
            }
        });

        log.info("Inbound message processed: conversationId={}, messageId={}",
            dbResult.getConversationId(), dbResult.getMessageId());
        return new InboundResult(true, dbResult.getConversationId().toString(), dbResult.getMessageId().toString(), null);
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
