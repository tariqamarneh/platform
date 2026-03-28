package com.businessagent.inbox.controller;

import com.businessagent.inbox.dto.response.InboundResult;
import com.businessagent.inbox.security.ApiKeyAuthFilter;
import com.businessagent.inbox.service.InboundService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InboundController.class)
@AutoConfigureMockMvc(addFilters = false)
class InboundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InboundService inboundService;

    @MockitoBean
    private ApiKeyAuthFilter apiKeyAuthFilter;

    private static final UUID CONVERSATION_ID = UUID.randomUUID();
    private static final UUID MESSAGE_ID = UUID.randomUUID();

    @Test
    void processInbound_validRequest_returns200() throws Exception {
        InboundResult result = new InboundResult(true, CONVERSATION_ID.toString(), MESSAGE_ID.toString(), null);
        when(inboundService.processInbound(any(), eq("test-api-key"))).thenReturn(result);

        String body = """
                {
                    "messageId": "wamid.test123",
                    "channelId": "%s",
                    "businessId": "%s",
                    "from": "5511999990000",
                    "customerName": "Test Customer",
                    "timestamp": 1700000000000,
                    "type": "text",
                    "payload": {"text": "Hello"}
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/api/v1/inbound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-Key", "test-api-key")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.conversationId").value(CONVERSATION_ID.toString()))
                .andExpect(jsonPath("$.messageId").value(MESSAGE_ID.toString()));
    }
}
