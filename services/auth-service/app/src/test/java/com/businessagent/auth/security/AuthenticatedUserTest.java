package com.businessagent.auth.security;

import com.businessagent.auth.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticatedUserTest {

    private AuthenticatedUser authenticatedUser;
    private UUID userId;
    private UUID businessId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        businessId = UUID.randomUUID();
        authenticatedUser = new AuthenticatedUser(userId, businessId, "test@example.com", Role.OWNER);
    }

    @Test
    void getAuthorities_shouldReturnCorrectRole() {
        Collection<? extends GrantedAuthority> authorities = authenticatedUser.getAuthorities();

        assertEquals(1, authorities.size());
        assertEquals("ROLE_OWNER", authorities.iterator().next().getAuthority());
    }

    @Test
    void getUsername_shouldReturnEmail() {
        assertEquals("test@example.com", authenticatedUser.getUsername());
    }

    @Test
    void isAccountNonExpired_shouldReturnTrue() {
        assertTrue(authenticatedUser.isAccountNonExpired());
    }

    @Test
    void isAccountNonLocked_shouldReturnTrue() {
        assertTrue(authenticatedUser.isAccountNonLocked());
    }

    @Test
    void isCredentialsNonExpired_shouldReturnTrue() {
        assertTrue(authenticatedUser.isCredentialsNonExpired());
    }

    @Test
    void isEnabled_shouldReturnTrue() {
        assertTrue(authenticatedUser.isEnabled());
    }

    @Test
    void getUserId_shouldReturnCorrectValue() {
        assertEquals(userId, authenticatedUser.getUserId());
    }

    @Test
    void getBusinessId_shouldReturnCorrectValue() {
        assertEquals(businessId, authenticatedUser.getBusinessId());
    }

    @Test
    void getRole_shouldReturnCorrectValue() {
        assertEquals(Role.OWNER, authenticatedUser.getRole());
    }
}
