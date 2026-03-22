package com.businessagent.auth.dto.response;

import com.businessagent.auth.model.enums.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        Role role,
        LocalDateTime createdAt
) {}
