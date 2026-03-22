package com.businessagent.auth.dto.response;

import com.businessagent.auth.model.enums.Role;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        Role role,
        UUID businessId
) {
}
