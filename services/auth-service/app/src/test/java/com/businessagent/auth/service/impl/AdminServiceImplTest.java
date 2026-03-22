package com.businessagent.auth.service.impl;

import com.businessagent.auth.config.JwtProperties;
import com.businessagent.auth.dto.request.LoginRequest;
import com.businessagent.auth.dto.request.UpdateBusinessRequest;
import com.businessagent.auth.dto.response.AdminAuthResponse;
import com.businessagent.auth.dto.response.AdminBusinessResponse;
import com.businessagent.auth.dto.response.AdminStatsResponse;
import com.businessagent.auth.dto.response.AdminUserResponse;
import com.businessagent.auth.exception.InvalidCredentialsException;
import com.businessagent.auth.exception.ResourceNotFoundException;
import com.businessagent.auth.model.Business;
import com.businessagent.auth.model.SuperAdmin;
import com.businessagent.auth.model.User;
import com.businessagent.auth.model.enums.BusinessStatus;
import com.businessagent.auth.model.enums.Plan;
import com.businessagent.auth.model.enums.Role;
import com.businessagent.auth.repository.BusinessRepository;
import com.businessagent.auth.repository.SuperAdminRepository;
import com.businessagent.auth.repository.UserRepository;
import com.businessagent.auth.security.JwtProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private SuperAdminRepository superAdminRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private AdminServiceImpl adminService;

    private static final UUID ADMIN_ID = UUID.randomUUID();
    private static final UUID BUSINESS_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    @Test
    void login_success_shouldReturnAdminAuthResponse() {
        LoginRequest request = new LoginRequest("admin@test.com", "password123");
        SuperAdmin admin = buildSuperAdmin();

        when(superAdminRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("password123", admin.getPasswordHash())).thenReturn(true);
        when(jwtProvider.generateAccessToken(admin)).thenReturn("token");
        when(jwtProperties.accessTokenExpiry()).thenReturn(Duration.ofMinutes(15));

        AdminAuthResponse response = adminService.login(request);

        assertNotNull(response);
        assertEquals("token", response.accessToken());
        assertEquals(900, response.expiresIn());
    }

    @Test
    void login_wrongEmail_shouldThrowInvalidCredentialsException() {
        LoginRequest request = new LoginRequest("nonexistent@test.com", "password123");
        when(superAdminRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> adminService.login(request));
    }

    @Test
    void login_wrongPassword_shouldThrowInvalidCredentialsException() {
        LoginRequest request = new LoginRequest("admin@test.com", "wrong_password");
        SuperAdmin admin = buildSuperAdmin();

        when(superAdminRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("wrong_password", admin.getPasswordHash())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> adminService.login(request));
    }

    @Test
    void getBusinesses_returnsPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Business business = buildBusiness();
        Page<Business> businessPage = new PageImpl<>(List.of(business));

        when(businessRepository.findAll(pageable)).thenReturn(businessPage);
        List<Object[]> counts = new java.util.ArrayList<>();
        counts.add(new Object[]{business.getId(), 5L});
        when(userRepository.countByBusinessIds(List.of(business.getId()))).thenReturn(counts);

        Page<AdminBusinessResponse> result = adminService.getBusinesses(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(business.getName(), result.getContent().get(0).name());
        assertEquals(5L, result.getContent().get(0).userCount());
    }

    @Test
    void getBusiness_found_shouldReturnResponse() {
        Business business = buildBusiness();

        when(businessRepository.findById(BUSINESS_ID)).thenReturn(Optional.of(business));
        when(userRepository.countByBusinessId(BUSINESS_ID)).thenReturn(3L);

        AdminBusinessResponse response = adminService.getBusiness(BUSINESS_ID);

        assertNotNull(response);
        assertEquals(BUSINESS_ID, response.id());
        assertEquals(business.getName(), response.name());
        assertEquals(3L, response.userCount());
    }

    @Test
    void getBusiness_notFound_shouldThrowResourceNotFoundException() {
        when(businessRepository.findById(BUSINESS_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.getBusiness(BUSINESS_ID));
    }

    @Test
    void updateBusiness_updatesPlan() {
        Business business = buildBusiness();
        UpdateBusinessRequest request = new UpdateBusinessRequest(Plan.PAID, null);

        when(businessRepository.findById(BUSINESS_ID)).thenReturn(Optional.of(business));
        when(businessRepository.save(any(Business.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.countByBusinessId(BUSINESS_ID)).thenReturn(0L);

        AdminBusinessResponse response = adminService.updateBusiness(BUSINESS_ID, request);

        assertNotNull(response);
        assertEquals(Plan.PAID, response.plan());
        verify(businessRepository).save(business);
    }

    @Test
    void updateBusiness_updatesStatus() {
        Business business = buildBusiness();
        UpdateBusinessRequest request = new UpdateBusinessRequest(null, BusinessStatus.SUSPENDED);

        when(businessRepository.findById(BUSINESS_ID)).thenReturn(Optional.of(business));
        when(businessRepository.save(any(Business.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.countByBusinessId(BUSINESS_ID)).thenReturn(0L);

        AdminBusinessResponse response = adminService.updateBusiness(BUSINESS_ID, request);

        assertNotNull(response);
        assertEquals(BusinessStatus.SUSPENDED, response.status());
        verify(businessRepository).save(business);
    }

    @Test
    void updateBusiness_notFound_shouldThrowResourceNotFoundException() {
        UpdateBusinessRequest request = new UpdateBusinessRequest(Plan.PAID, null);
        when(businessRepository.findById(BUSINESS_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> adminService.updateBusiness(BUSINESS_ID, request));
    }

    @Test
    void getBusinessUsers_returnsPage() {
        Pageable pageable = PageRequest.of(0, 20);
        User user = buildUser();
        Page<User> userPage = new PageImpl<>(List.of(user));

        when(businessRepository.existsById(BUSINESS_ID)).thenReturn(true);
        when(userRepository.findByBusinessId(BUSINESS_ID, pageable)).thenReturn(userPage);

        Page<AdminUserResponse> result = adminService.getBusinessUsers(BUSINESS_ID, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(user.getEmail(), result.getContent().get(0).email());
        assertEquals(user.getFirstName(), result.getContent().get(0).firstName());
    }

    @Test
    void getBusinessUsers_businessNotFound_shouldThrowResourceNotFoundException() {
        Pageable pageable = PageRequest.of(0, 20);
        when(businessRepository.existsById(BUSINESS_ID)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> adminService.getBusinessUsers(BUSINESS_ID, pageable));
    }

    @Test
    void getStats_returnsCounts() {
        when(businessRepository.count()).thenReturn(10L);
        when(businessRepository.countByStatus(BusinessStatus.ACTIVE)).thenReturn(7L);
        when(businessRepository.countByStatus(BusinessStatus.SUSPENDED)).thenReturn(3L);
        when(userRepository.count()).thenReturn(50L);
        when(businessRepository.countByPlan(Plan.FREE)).thenReturn(6L);
        when(businessRepository.countByPlan(Plan.PAID)).thenReturn(4L);

        AdminStatsResponse response = adminService.getStats();

        assertNotNull(response);
        assertEquals(10L, response.totalBusinesses());
        assertEquals(7L, response.activeBusinesses());
        assertEquals(3L, response.suspendedBusinesses());
        assertEquals(50L, response.totalUsers());
        assertEquals(6L, response.freeBusinesses());
        assertEquals(4L, response.paidBusinesses());
    }

    private SuperAdmin buildSuperAdmin() {
        SuperAdmin admin = new SuperAdmin();
        admin.setId(ADMIN_ID);
        admin.setEmail("admin@test.com");
        admin.setPasswordHash("encoded_password");
        return admin;
    }

    private Business buildBusiness() {
        Business business = new Business();
        business.setId(BUSINESS_ID);
        business.setName("Test Business");
        business.setPlan(Plan.FREE);
        business.setStatus(BusinessStatus.ACTIVE);
        business.setCreatedAt(LocalDateTime.now());
        business.setUpdatedAt(LocalDateTime.now());
        return business;
    }

    private User buildUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setBusinessId(BUSINESS_ID);
        user.setEmail("user@test.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(Role.OWNER);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}
