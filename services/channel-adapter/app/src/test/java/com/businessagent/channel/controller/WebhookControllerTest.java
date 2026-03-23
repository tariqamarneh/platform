package com.businessagent.channel.controller;

import com.businessagent.channel.exception.GlobalExceptionHandler;
import com.businessagent.channel.security.WebhookSignatureValidator;
import com.businessagent.channel.service.WebhookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WebhookController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WebhookService webhookService;

    @MockitoBean
    private WebhookSignatureValidator webhookSignatureValidator;

    @Test
    void verifyWebhook_shouldReturn200WithChallenge() throws Exception {
        when(webhookService.verifyWebhook("token-abc", "subscribe", "challenge-123", "verify-token"))
                .thenReturn("challenge-123");

        mockMvc.perform(get("/webhook/{token}", "token-abc")
                        .param("hub.mode", "subscribe")
                        .param("hub.challenge", "challenge-123")
                        .param("hub.verify_token", "verify-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("challenge-123"));
    }

    @Test
    void receiveWebhook_shouldReturn200() throws Exception {
        doNothing().when(webhookSignatureValidator).validate(any(), any());
        doNothing().when(webhookService).processWebhook(any(), any());

        String body = """
                {
                    "object": "whatsapp_business_account",
                    "entry": []
                }
                """;

        mockMvc.perform(post("/webhook/{token}", "token-abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Hub-Signature-256", "sha256=abc123")
                        .content(body))
                .andExpect(status().isOk());
    }
}
