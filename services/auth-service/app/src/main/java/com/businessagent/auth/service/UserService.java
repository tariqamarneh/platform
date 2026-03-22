package com.businessagent.auth.service;

import com.businessagent.auth.dto.response.UserResponse;

import java.util.UUID;

public interface UserService {

    UserResponse getCurrentUser(UUID userId);
}
