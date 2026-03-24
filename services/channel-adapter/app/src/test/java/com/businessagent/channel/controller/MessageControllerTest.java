package com.businessagent.channel.controller;

import com.businessagent.channel.dto.response.SendMessageResponse;
import com.businessagent.channel.exception.GlobalExceptionHandler;
import com.businessagent.channel.security.WebhookSignatureValidator;
import com.businessagent.channel.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.businessagent.channel.security.ApiKeyAuthFilter;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MessageService messageService;

    @MockitoBean
    private ApiKeyAuthFilter apiKeyAuthFilter;

    @MockitoBean
    private WebhookSignatureValidator webhookSignatureValidator;

    private static final UUID CHANNEL_ID = UUID.randomUUID();

    @Test
    void sendMessage_shouldReturn200() throws Exception {
        SendMessageResponse response = new SendMessageResponse(true, "wamid.123", null);
        when(messageService.sendMessage(any())).thenReturn(response);

        String body = """
                {
                    "channelId": "%s",
                    "to": "5511999990000",
                    "type": "text",
                    "content": {"body": "Hello"}
                }
                """.formatted(CHANNEL_ID);

        mockMvc.perform(post("/api/v1/messages/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.messageId").value("wamid.123"));
    }

    @Test
    void sendTypingIndicator_shouldReturn200() throws Exception {
        doNothing().when(messageService).sendTypingIndicator(any());

        String body = """
                {
                    "channelId": "%s",
                    "to": "5511999990000"
                }
                """.formatted(CHANNEL_ID);

        mockMvc.perform(post("/api/v1/messages/typing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void markAsRead_shouldReturn200() throws Exception {
        doNothing().when(messageService).markAsRead(any());

        String body = """
                {
                    "channelId": "%s",
                    "messageId": "wamid.456"
                }
                """.formatted(CHANNEL_ID);

        mockMvc.perform(post("/api/v1/messages/read")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }
}
