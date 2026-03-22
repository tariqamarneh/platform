package com.businessagent.auth.controller;

import com.businessagent.auth.dto.response.AdminAuthResponse;
import com.businessagent.auth.dto.response.AdminBusinessResponse;
import com.businessagent.auth.dto.response.AdminStatsResponse;
import com.businessagent.auth.dto.response.AdminUserResponse;
import com.businessagent.auth.exception.GlobalExceptionHandler;
import com.businessagent.auth.model.enums.BusinessStatus;
import com.businessagent.auth.model.enums.Plan;
import com.businessagent.auth.model.enums.Role;
import com.businessagent.auth.security.AuthenticatedUser;
import com.businessagent.auth.security.JwtAuthenticationFilter;
import com.businessagent.auth.service.AdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final UUID BUSINESS_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        AuthenticatedUser admin = new AuthenticatedUser(
                UUID.randomUUID(), null, "admin@test.com", Role.SUPER_ADMIN);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities()));
    }

    @Test
    void login_validBody_returns200() throws Exception {
        AdminAuthResponse authResponse = new AdminAuthResponse("token", 900);
        when(adminService.login(any())).thenReturn(authResponse);

        String body = """
                {
                    "email": "admin@test.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/v1/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token"))
                .andExpect(jsonPath("$.expiresIn").value(900));
    }

    @Test
    void getBusinesses_returns200() throws Exception {
        AdminBusinessResponse businessResponse = new AdminBusinessResponse(
                BUSINESS_ID, "Test Biz", Plan.FREE, BusinessStatus.ACTIVE,
                5L, LocalDateTime.now(), LocalDateTime.now());
        Page<AdminBusinessResponse> page = new PageImpl<>(List.of(businessResponse));
        when(adminService.getBusinesses(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/businesses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test Biz"))
                .andExpect(jsonPath("$.content[0].userCount").value(5));
    }

    @Test
    void getBusiness_returns200() throws Exception {
        AdminBusinessResponse businessResponse = new AdminBusinessResponse(
                BUSINESS_ID, "Test Biz", Plan.FREE, BusinessStatus.ACTIVE,
                3L, LocalDateTime.now(), LocalDateTime.now());
        when(adminService.getBusiness(BUSINESS_ID)).thenReturn(businessResponse);

        mockMvc.perform(get("/api/v1/admin/businesses/{id}", BUSINESS_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Biz"))
                .andExpect(jsonPath("$.userCount").value(3));
    }

    @Test
    void updateBusiness_returns200() throws Exception {
        AdminBusinessResponse businessResponse = new AdminBusinessResponse(
                BUSINESS_ID, "Test Biz", Plan.PAID, BusinessStatus.ACTIVE,
                0L, LocalDateTime.now(), LocalDateTime.now());
        when(adminService.updateBusiness(eq(BUSINESS_ID), any())).thenReturn(businessResponse);

        String body = """
                {
                    "plan": "PAID"
                }
                """;

        mockMvc.perform(patch("/api/v1/admin/businesses/{id}", BUSINESS_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plan").value("PAID"));
    }

    @Test
    void getBusinessUsers_returns200() throws Exception {
        AdminUserResponse userResponse = new AdminUserResponse(
                UUID.randomUUID(), "user@test.com", "John", "Doe",
                Role.OWNER, LocalDateTime.now());
        Page<AdminUserResponse> page = new PageImpl<>(List.of(userResponse));
        when(adminService.getBusinessUsers(eq(BUSINESS_ID), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/businesses/{id}/users", BUSINESS_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("user@test.com"))
                .andExpect(jsonPath("$.content[0].firstName").value("John"));
    }

    @Test
    void getStats_returns200() throws Exception {
        AdminStatsResponse statsResponse = new AdminStatsResponse(10, 7, 3, 50, 6, 4);
        when(adminService.getStats()).thenReturn(statsResponse);

        mockMvc.perform(get("/api/v1/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBusinesses").value(10))
                .andExpect(jsonPath("$.activeBusinesses").value(7))
                .andExpect(jsonPath("$.suspendedBusinesses").value(3))
                .andExpect(jsonPath("$.totalUsers").value(50))
                .andExpect(jsonPath("$.freeBusinesses").value(6))
                .andExpect(jsonPath("$.paidBusinesses").value(4));
    }
}
