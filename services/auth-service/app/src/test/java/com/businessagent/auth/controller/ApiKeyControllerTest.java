package com.businessagent.auth.controller;

import com.businessagent.auth.dto.response.ApiKeyCreatedResponse;
import com.businessagent.auth.dto.response.ApiKeyResponse;
import com.businessagent.auth.dto.response.ApiKeyVerifyResponse;
import com.businessagent.auth.model.enums.Role;
import com.businessagent.auth.security.AuthenticatedUser;
import com.businessagent.auth.security.JwtAuthenticationFilter;
import com.businessagent.auth.service.ApiKeyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApiKeyController.class)
@AutoConfigureMockMvc(addFilters = false)
class ApiKeyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ApiKeyService apiKeyService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID BUSINESS_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(USER_ID, BUSINESS_ID, "test@test.com", Role.OWNER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(authenticatedUser, null, authenticatedUser.getAuthorities())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createKey_withValidBody_shouldReturn201() throws Exception {
        ApiKeyCreatedResponse response = new ApiKeyCreatedResponse(
                UUID.randomUUID(), "My Key", "ba_live_abcd", "ba_live_abcdef1234567890", LocalDateTime.now()
        );
        when(apiKeyService.createKey(eq(BUSINESS_ID), any())).thenReturn(response);

        String body = """
                {
                    "name": "My Key"
                }
                """;

        mockMvc.perform(post("/api/v1/keys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("My Key"))
                .andExpect(jsonPath("$.rawKey").value("ba_live_abcdef1234567890"));
    }

    @Test
    void verifyKey_shouldReturn200() throws Exception {
        ApiKeyVerifyResponse response = new ApiKeyVerifyResponse(true, BUSINESS_ID);
        when(apiKeyService.verifyKey("somekey")).thenReturn(response);

        mockMvc.perform(post("/api/v1/keys/verify")
                        .header("X-API-Key", "somekey"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.businessId").value(BUSINESS_ID.toString()));
    }

    @Test
    void revokeKey_shouldReturn204() throws Exception {
        UUID keyId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/keys/{id}", keyId))
                .andExpect(status().isNoContent());

        verify(apiKeyService).revokeKey(keyId, BUSINESS_ID);
    }

    @Test
    void listKeys_shouldReturn200() throws Exception {
        ApiKeyResponse resp1 = new ApiKeyResponse(UUID.randomUUID(), "Key 1", "ba_live_1234", LocalDateTime.now());
        ApiKeyResponse resp2 = new ApiKeyResponse(UUID.randomUUID(), "Key 2", "ba_live_5678", LocalDateTime.now());
        when(apiKeyService.listKeys(BUSINESS_ID)).thenReturn(List.of(resp1, resp2));

        mockMvc.perform(get("/api/v1/keys"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Key 1"))
                .andExpect(jsonPath("$[1].name").value("Key 2"));
    }
}
