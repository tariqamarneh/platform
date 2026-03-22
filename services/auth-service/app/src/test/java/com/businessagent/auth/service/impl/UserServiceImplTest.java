package com.businessagent.auth.service.impl;

import com.businessagent.auth.converter.UserConverter;
import com.businessagent.auth.dto.response.UserResponse;
import com.businessagent.auth.exception.ResourceNotFoundException;
import com.businessagent.auth.model.User;
import com.businessagent.auth.model.enums.Role;
import com.businessagent.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserConverter userConverter;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getCurrentUser_happyPath_shouldReturnUserResponse() {
        UUID userId = UUID.randomUUID();
        UUID businessId = UUID.randomUUID();
        User user = buildUser(userId, businessId);
        UserResponse expectedResponse = new UserResponse(userId, "test@example.com", "John", "Doe", Role.OWNER, businessId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userConverter.toResponse(user)).thenReturn(expectedResponse);

        UserResponse response = userService.getCurrentUser(userId);

        assertNotNull(response);
        assertEquals(userId, response.id());
        assertEquals("test@example.com", response.email());
        assertEquals("John", response.firstName());
        assertEquals("Doe", response.lastName());
        assertEquals(Role.OWNER, response.role());
        assertEquals(businessId, response.businessId());
    }

    @Test
    void getCurrentUser_notFound_shouldThrowResourceNotFoundException() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getCurrentUser(userId));
    }

    private User buildUser(UUID userId, UUID businessId) {
        User user = new User();
        user.setId(userId);
        user.setBusinessId(businessId);
        user.setEmail("test@example.com");
        user.setPasswordHash("encoded_password");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(Role.OWNER);
        return user;
    }
}
