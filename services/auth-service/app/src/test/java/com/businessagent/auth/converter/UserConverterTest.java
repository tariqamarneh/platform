package com.businessagent.auth.converter;

import com.businessagent.auth.dto.response.UserResponse;
import com.businessagent.auth.model.User;
import com.businessagent.auth.model.enums.Role;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserConverterTest {

    private final UserConverter userConverter = new UserConverter();

    @Test
    void toResponse_shouldMapAllFieldsCorrectly() {
        UUID userId = UUID.randomUUID();
        UUID businessId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setBusinessId(businessId);
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(Role.ADMIN);
        user.setPasswordHash("hash");

        UserResponse response = userConverter.toResponse(user);

        assertNotNull(response);
        assertEquals(userId, response.id());
        assertEquals("test@example.com", response.email());
        assertEquals("John", response.firstName());
        assertEquals("Doe", response.lastName());
        assertEquals(Role.ADMIN, response.role());
        assertEquals(businessId, response.businessId());
    }
}
