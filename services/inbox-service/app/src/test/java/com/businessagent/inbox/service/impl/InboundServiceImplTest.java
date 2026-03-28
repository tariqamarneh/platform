package com.businessagent.inbox.service.impl;

import com.businessagent.inbox.client.AiEngineClient;
import com.businessagent.inbox.client.ChannelAdapterClient;
import com.businessagent.inbox.dto.request.InboundMessageRequest;
import com.businessagent.inbox.dto.response.InboundResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InboundServiceImplTest {

    @Mock
    private InboundPersistenceService persistenceService;

    @Mock
    private AiEngineClient aiEngineClient;

    @Mock
    private ChannelAdapterClient channelAdapterClient;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private InboundServiceImpl inboundService;

    private static final UUID BUSINESS_ID = UUID.randomUUID();
    private static final UUID CHANNEL_ID = UUID.randomUUID();
    private static final UUID CONTACT_ID = UUID.randomUUID();
    private static final UUID CONVERSATION_ID = UUID.randomUUID();
    private static final UUID MESSAGE_ID = UUID.randomUUID();
    private static final String API_KEY = "test-api-key";

    @Test
    void processInbound_success_returnsSuccessResult() {
        InboundMessageRequest request = buildInboundRequest();
        var saveResult = new InboundPersistenceService.SaveResult(
                CONVERSATION_ID, MESSAGE_ID, CONTACT_ID, "Test Customer");
        when(persistenceService.saveInbound(request)).thenReturn(saveResult);

        InboundResult result = inboundService.processInbound(request, API_KEY);

        assertTrue(result.success());
        assertEquals(CONVERSATION_ID.toString(), result.conversationId());
        assertEquals(MESSAGE_ID.toString(), result.messageId());
        assertNull(result.error());

        verify(persistenceService).saveInbound(request);
    }

    @Test
    void processInbound_persistenceFailure_returnsErrorResult() {
        InboundMessageRequest request = buildInboundRequest();
        when(persistenceService.saveInbound(request)).thenThrow(new RuntimeException("DB error"));

        InboundResult result = inboundService.processInbound(request, API_KEY);

        assertFalse(result.success());
        assertNull(result.conversationId());
        assertNull(result.messageId());
        assertNotNull(result.error());
    }

    @Test
    void processInbound_invalidRequest_returnsErrorResult() {
        InboundMessageRequest request = buildInboundRequest();
        when(persistenceService.saveInbound(request)).thenThrow(new IllegalArgumentException("Invalid UUID"));

        InboundResult result = inboundService.processInbound(request, API_KEY);

        assertFalse(result.success());
        assertNull(result.conversationId());
        assertNull(result.messageId());
        assertTrue(result.error().contains("Invalid request"));
    }

    private InboundMessageRequest buildInboundRequest() {
        return new InboundMessageRequest(
                "wamid.test123",
                CHANNEL_ID.toString(),
                BUSINESS_ID.toString(),
                "5511999990000",
                "Test Customer",
                System.currentTimeMillis(),
                "text",
                Map.of("text", "Hello"),
                null
        );
    }
}
