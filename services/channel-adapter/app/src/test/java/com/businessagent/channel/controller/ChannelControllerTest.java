package com.businessagent.channel.controller;

import com.businessagent.channel.dto.response.ChannelCreatedResponse;
import com.businessagent.channel.dto.response.ChannelResponse;
import com.businessagent.channel.exception.GlobalExceptionHandler;
import com.businessagent.channel.model.enums.ChannelProvider;
import com.businessagent.channel.model.enums.ChannelStatus;
import com.businessagent.channel.security.WebhookSignatureValidator;
import com.businessagent.channel.service.ChannelService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChannelController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ChannelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChannelService channelService;

    @MockitoBean
    private ApiKeyAuthFilter apiKeyAuthFilter;

    @MockitoBean
    private WebhookSignatureValidator webhookSignatureValidator;

    private static final UUID CHANNEL_ID = UUID.randomUUID();
    private static final UUID BUSINESS_ID = UUID.randomUUID();

    @Test
    void createChannel_shouldReturn201() throws Exception {
        when(channelService.createChannel(any())).thenReturn(buildChannelCreatedResponse());

        String body = """
                {
                    "businessId": "%s",
                    "provider": "WHATSAPP",
                    "displayName": "My Channel",
                    "phoneNumber": "+5511999990000",
                    "phoneNumberId": "phone-id-1",
                    "wabaId": "waba-1",
                    "apiKey": "api-key-123"
                }
                """.formatted(BUSINESS_ID);

        mockMvc.perform(post("/api/v1/channels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(CHANNEL_ID.toString()))
                .andExpect(jsonPath("$.displayName").value("My Channel"));
    }

    @Test
    void getChannel_shouldReturn200() throws Exception {
        when(channelService.getChannel(CHANNEL_ID)).thenReturn(buildChannelResponse());

        mockMvc.perform(get("/api/v1/channels/{id}", CHANNEL_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CHANNEL_ID.toString()));
    }

    @Test
    void getChannelsByBusiness_shouldReturn200() throws Exception {
        when(channelService.getChannelsByBusiness(BUSINESS_ID))
                .thenReturn(List.of(buildChannelResponse()));

        mockMvc.perform(get("/api/v1/channels/business/{businessId}", BUSINESS_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void updateChannel_shouldReturn200() throws Exception {
        when(channelService.updateChannel(eq(CHANNEL_ID), any())).thenReturn(buildChannelResponse());

        String body = """
                {
                    "displayName": "Updated Name"
                }
                """;

        mockMvc.perform(patch("/api/v1/channels/{id}", CHANNEL_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CHANNEL_ID.toString()));
    }

    @Test
    void deactivateChannel_shouldReturn204() throws Exception {
        doNothing().when(channelService).deactivateChannel(CHANNEL_ID);

        mockMvc.perform(delete("/api/v1/channels/{id}", CHANNEL_ID))
                .andExpect(status().isNoContent());
    }

    private ChannelResponse buildChannelResponse() {
        return new ChannelResponse(
                CHANNEL_ID, BUSINESS_ID, ChannelProvider.WHATSAPP,
                "My Channel", "+5511999990000", "phone-id-1", "waba-1",
                null, null,
                ChannelStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());
    }

    private ChannelCreatedResponse buildChannelCreatedResponse() {
        return new ChannelCreatedResponse(
                CHANNEL_ID, BUSINESS_ID, ChannelProvider.WHATSAPP,
                "My Channel", "+5511999990000", "phone-id-1", "waba-1",
                null, null,
                "webhook-token", ChannelStatus.ACTIVE, LocalDateTime.now());
    }
}
