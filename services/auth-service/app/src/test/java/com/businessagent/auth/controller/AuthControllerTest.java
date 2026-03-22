package com.businessagent.auth.controller;

import com.businessagent.auth.dto.response.AuthResponse;
import com.businessagent.auth.exception.GlobalExceptionHandler;
import com.businessagent.auth.security.JwtAuthenticationFilter;
import com.businessagent.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void register_withValidBody_shouldReturn201() throws Exception {
        AuthResponse authResponse = new AuthResponse("access_token", "refresh_token", 900);
        when(authService.register(any())).thenReturn(authResponse);

        String body = """
                {
                    "businessName": "Test Business",
                    "email": "test@example.com",
                    "password": "password123",
                    "firstName": "John",
                    "lastName": "Doe"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access_token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh_token"))
                .andExpect(jsonPath("$.expiresIn").value(900));
    }

    @Test
    void register_withMissingFields_shouldReturn400() throws Exception {
        String body = """
                {
                    "email": "test@example.com"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withInvalidEmail_shouldReturn400() throws Exception {
        String body = """
                {
                    "businessName": "Test Business",
                    "email": "not-an-email",
                    "password": "password123",
                    "firstName": "John",
                    "lastName": "Doe"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withShortPassword_shouldReturn400() throws Exception {
        String body = """
                {
                    "businessName": "Test Business",
                    "email": "test@example.com",
                    "password": "short",
                    "firstName": "John",
                    "lastName": "Doe"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withValidBody_shouldReturn200() throws Exception {
        AuthResponse authResponse = new AuthResponse("access_token", "refresh_token", 900);
        when(authService.login(any())).thenReturn(authResponse);

        String body = """
                {
                    "email": "test@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access_token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh_token"));
    }

    @Test
    void refresh_withValidBody_shouldReturn200() throws Exception {
        AuthResponse authResponse = new AuthResponse("new_access_token", "new_refresh_token", 900);
        when(authService.refresh(any())).thenReturn(authResponse);

        String body = """
                {
                    "refreshToken": "some_refresh_token"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new_access_token"))
                .andExpect(jsonPath("$.refreshToken").value("new_refresh_token"));
    }
}
