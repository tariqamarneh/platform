package com.businessagent.auth.security;

import com.businessagent.auth.config.JwtProperties;
import com.businessagent.auth.model.User;
import com.businessagent.auth.model.enums.Role;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtProviderTest {

    private static final String SECRET = "this-is-a-test-secret-key-that-is-at-least-256-bits-long-for-hmac";

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(SECRET, Duration.ofMinutes(15), Duration.ofDays(7));
        jwtProvider = new JwtProvider(properties);
        jwtProvider.init();
    }

    @Test
    void generateAccessToken_shouldCreateValidJwt() {
        User user = buildTestUser();

        String token = jwtProvider.generateAccessToken(user);

        assertNotNull(token);
        assertTrue(jwtProvider.parseAccessToken(token).isPresent());
    }

    @Test
    void parseAccessToken_shouldReturnCorrectClaims() {
        User user = buildTestUser();
        String token = jwtProvider.generateAccessToken(user);

        Optional<Claims> optionalClaims = jwtProvider.parseAccessToken(token);

        assertTrue(optionalClaims.isPresent());
        Claims claims = optionalClaims.get();
        assertEquals(user.getId().toString(), claims.getSubject());
        assertEquals(user.getBusinessId().toString(), claims.get("businessId", String.class));
        assertEquals(user.getRole().name(), claims.get("role", String.class));
        assertEquals(user.getEmail(), claims.get("email", String.class));
    }

    @Test
    void parseAccessToken_shouldReturnEmptyForExpiredToken() throws InterruptedException {
        JwtProperties shortExpiry = new JwtProperties(SECRET, Duration.ofMillis(1), Duration.ofDays(7));
        JwtProvider shortLivedProvider = new JwtProvider(shortExpiry);
        shortLivedProvider.init();
        User user = buildTestUser();

        String token = shortLivedProvider.generateAccessToken(user);
        Thread.sleep(50);

        assertTrue(shortLivedProvider.parseAccessToken(token).isEmpty());
    }

    @Test
    void parseAccessToken_shouldReturnEmptyForTamperedToken() {
        User user = buildTestUser();
        String token = jwtProvider.generateAccessToken(user);
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

        assertTrue(jwtProvider.parseAccessToken(tamperedToken).isEmpty());
    }

    @Test
    void parseAccessToken_shouldReturnEmptyForRandomString() {
        assertTrue(jwtProvider.parseAccessToken("not.a.valid.jwt.token").isEmpty());
    }

    @Test
    void generateRefreshToken_shouldReturnNonEmptyUniqueStrings() {
        Set<String> tokens = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            String token = jwtProvider.generateRefreshToken();
            assertNotNull(token);
            assertFalse(token.isEmpty());
            tokens.add(token);
        }

        assertEquals(50, tokens.size());
    }

    private User buildTestUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setBusinessId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setRole(Role.OWNER);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPasswordHash("hashed");
        return user;
    }
}
