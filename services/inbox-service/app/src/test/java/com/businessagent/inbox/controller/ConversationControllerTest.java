package com.businessagent.inbox.controller;

import com.businessagent.inbox.dto.response.ConversationResponse;
import com.businessagent.inbox.dto.response.MessageResponse;
import com.businessagent.inbox.model.enums.AssigneeType;
import com.businessagent.inbox.model.enums.ConversationStatus;
import com.businessagent.inbox.model.enums.MessageActorType;
import com.businessagent.inbox.model.enums.MessageContentType;
import com.businessagent.inbox.model.enums.MessageDirection;
import com.businessagent.inbox.model.enums.MessageStatus;
import com.businessagent.inbox.security.ApiKeyAuthFilter;
import com.businessagent.inbox.service.ConversationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConversationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ConversationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ConversationService conversationService;

    @MockitoBean
    private ApiKeyAuthFilter apiKeyAuthFilter;

    private static final UUID BUSINESS_ID = UUID.randomUUID();
    private static final UUID CHANNEL_ID = UUID.randomUUID();
    private static final UUID CONTACT_ID = UUID.randomUUID();
    private static final UUID CONVERSATION_ID = UUID.randomUUID();
    private static final UUID MESSAGE_ID = UUID.randomUUID();

    @Test
    void getConversation_returns200() throws Exception {
        ConversationResponse response = buildConversationResponse();
        when(conversationService.getConversation(CONVERSATION_ID)).thenReturn(response);

        mockMvc.perform(get("/api/v1/conversations/{id}", CONVERSATION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CONVERSATION_ID.toString()))
                .andExpect(jsonPath("$.businessId").value(BUSINESS_ID.toString()))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void getMessages_returns200() throws Exception {
        MessageResponse messageResponse = buildMessageResponse();
        Page<MessageResponse> page = new PageImpl<>(List.of(messageResponse), PageRequest.of(0, 50), 1);
        when(conversationService.getConversationMessages(eq(CONVERSATION_ID), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/conversations/{id}/messages", CONVERSATION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(MESSAGE_ID.toString()))
                .andExpect(jsonPath("$.content[0].direction").value("INBOUND"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    private ConversationResponse buildConversationResponse() {
        return new ConversationResponse(
                CONVERSATION_ID, BUSINESS_ID, CHANNEL_ID, CONTACT_ID,
                ConversationStatus.OPEN, AssigneeType.AI_BOT,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private MessageResponse buildMessageResponse() {
        return new MessageResponse(
                MESSAGE_ID, CONVERSATION_ID, MessageDirection.INBOUND, MessageActorType.CONTACT,
                MessageContentType.TEXT, Map.of("text", "Hello"), "wamid.test123",
                MessageStatus.DELIVERED, LocalDateTime.now());
    }
}
