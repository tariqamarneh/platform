package com.businessagent.inbox.controller;

import com.businessagent.inbox.dto.response.ReplyResult;
import com.businessagent.inbox.security.ApiKeyAuthFilter;
import com.businessagent.inbox.service.ReplyService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReplyController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReplyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReplyService replyService;

    @MockitoBean
    private ApiKeyAuthFilter apiKeyAuthFilter;

    private static final UUID CONVERSATION_ID = UUID.randomUUID();
    private static final UUID MESSAGE_ID = UUID.randomUUID();

    @Test
    void sendReply_returns200() throws Exception {
        ReplyResult result = new ReplyResult(true, MESSAGE_ID.toString(), null);
        when(replyService.sendReply(any())).thenReturn(result);

        String body = """
                {
                    "conversationId": "%s",
                    "businessId": "%s",
                    "messageType": "text",
                    "content": {"text": "Hello from AI"}
                }
                """.formatted(CONVERSATION_ID, UUID.randomUUID());

        mockMvc.perform(post("/api/v1/reply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.messageId").value(MESSAGE_ID.toString()));
    }

    @Test
    void setTyping_returns200() throws Exception {
        doNothing().when(replyService).setTypingIndicator(any());

        String body = """
                {
                    "conversationId": "%s"
                }
                """.formatted(CONVERSATION_ID);

        mockMvc.perform(post("/api/v1/typing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }
}
