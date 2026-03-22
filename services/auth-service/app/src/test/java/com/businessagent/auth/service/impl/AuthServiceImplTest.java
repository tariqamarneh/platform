package com.businessagent.auth.service.impl;

import com.businessagent.auth.config.JwtProperties;
import com.businessagent.auth.converter.UserConverter;
import com.businessagent.auth.dto.request.LoginRequest;
import com.businessagent.auth.dto.request.RefreshRequest;
import com.businessagent.auth.dto.request.RegisterRequest;
import com.businessagent.auth.dto.response.AuthResponse;
import com.businessagent.auth.exception.DuplicateResourceException;
import com.businessagent.auth.exception.InvalidCredentialsException;
import com.businessagent.auth.exception.InvalidTokenException;
import com.businessagent.auth.model.Business;
import com.businessagent.auth.model.RefreshToken;
import com.businessagent.auth.model.User;
import com.businessagent.auth.model.enums.Plan;
import com.businessagent.auth.model.enums.Role;
import com.businessagent.auth.repository.BusinessRepository;
import com.businessagent.auth.repository.RefreshTokenRepository;
import com.businessagent.auth.repository.UserRepository;
import com.businessagent.auth.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private UserConverter userConverter;

    @InjectMocks
    private AuthServiceImpl authService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID BUSINESS_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
    }

    @Test
    void register_happyPath_shouldCreateBusinessAndUserAndReturnTokens() {
        RegisterRequest request = new RegisterRequest("Test Biz", "test@example.com", "password123", "John", "Doe");
        Business savedBusiness = buildBusiness();
        User savedUser = buildUser();

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(businessRepository.save(any(Business.class))).thenReturn(savedBusiness);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(jwtProvider.generateAccessToken(any(User.class))).thenReturn("access_token");
        when(jwtProvider.generateRefreshToken()).thenReturn("refresh_token");
        when(jwtProperties.accessTokenExpiry()).thenReturn(Duration.ofMinutes(15));
        when(jwtProperties.refreshTokenExpiry()).thenReturn(Duration.ofDays(7));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("access_token", response.accessToken());
        assertEquals("refresh_token", response.refreshToken());
        assertEquals(900, response.expiresIn());

        verify(businessRepository).save(any(Business.class));
        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void register_duplicateEmail_shouldThrowDuplicateResourceException() {
        RegisterRequest request = new RegisterRequest("Test Biz", "test@example.com", "password123", "John", "Doe");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));

        verify(businessRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_happyPath_shouldReturnTokens() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        User user = buildUser();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", user.getPasswordHash())).thenReturn(true);
        when(jwtProvider.generateAccessToken(user)).thenReturn("access_token");
        when(jwtProvider.generateRefreshToken()).thenReturn("refresh_token");
        when(jwtProperties.accessTokenExpiry()).thenReturn(Duration.ofMinutes(15));
        when(jwtProperties.refreshTokenExpiry()).thenReturn(Duration.ofDays(7));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access_token", response.accessToken());
        assertEquals("refresh_token", response.refreshToken());
    }

    @Test
    void login_wrongEmail_shouldThrowInvalidCredentialsException() {
        LoginRequest request = new LoginRequest("nonexistent@example.com", "password123");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void login_wrongPassword_shouldThrowInvalidCredentialsException() {
        LoginRequest request = new LoginRequest("test@example.com", "wrong_password");
        User user = buildUser();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong_password", user.getPasswordHash())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void refresh_happyPath_shouldRevokeOldTokenAndReturnNewTokens() {
        RefreshRequest request = new RefreshRequest("old_refresh_token");
        RefreshToken storedToken = buildRefreshToken(false, LocalDateTime.now(Clock.systemUTC()).plusDays(1));
        User user = buildUser();

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedToken));
        when(userRepository.findById(storedToken.getUserId())).thenReturn(Optional.of(user));
        when(jwtProvider.generateAccessToken(user)).thenReturn("new_access_token");
        when(jwtProvider.generateRefreshToken()).thenReturn("new_refresh_token");
        when(jwtProperties.accessTokenExpiry()).thenReturn(Duration.ofMinutes(15));
        when(jwtProperties.refreshTokenExpiry()).thenReturn(Duration.ofDays(7));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = authService.refresh(request);

        assertNotNull(response);
        assertEquals("new_access_token", response.accessToken());
        assertEquals("new_refresh_token", response.refreshToken());

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        // The first save should be the revoked old token
        RefreshToken revokedToken = captor.getAllValues().stream()
                .filter(RefreshToken::isRevoked)
                .findFirst()
                .orElse(null);
        assertNotNull(revokedToken);
    }

    @Test
    void refresh_expiredToken_shouldThrowInvalidTokenException() {
        RefreshRequest request = new RefreshRequest("expired_token");
        RefreshToken expiredToken = buildRefreshToken(false, LocalDateTime.now(Clock.systemUTC()).minusDays(1));

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(expiredToken));

        assertThrows(InvalidTokenException.class, () -> authService.refresh(request));
    }

    @Test
    void refresh_revokedToken_shouldThrowInvalidTokenException() {
        RefreshRequest request = new RefreshRequest("revoked_token");
        RefreshToken revokedToken = buildRefreshToken(true, LocalDateTime.now(Clock.systemUTC()).plusDays(1));

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(revokedToken));

        assertThrows(InvalidTokenException.class, () -> authService.refresh(request));
    }

    @Test
    void refresh_invalidToken_shouldThrowInvalidTokenException() {
        RefreshRequest request = new RefreshRequest("unknown_token");

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () -> authService.refresh(request));
    }

    private User buildUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setBusinessId(BUSINESS_ID);
        user.setEmail("test@example.com");
        user.setPasswordHash("encoded_password");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(Role.OWNER);
        return user;
    }

    private Business buildBusiness() {
        Business business = new Business();
        business.setId(BUSINESS_ID);
        business.setName("Test Biz");
        business.setPlan(Plan.FREE);
        return business;
    }

    private RefreshToken buildRefreshToken(boolean revoked, LocalDateTime expiresAt) {
        RefreshToken token = new RefreshToken();
        token.setId(UUID.randomUUID());
        token.setUserId(USER_ID);
        token.setTokenHash("some_hash");
        token.setExpiresAt(expiresAt);
        token.setRevoked(revoked);
        return token;
    }
}
