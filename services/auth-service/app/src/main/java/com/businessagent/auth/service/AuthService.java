package com.businessagent.auth.service;

import com.businessagent.auth.dto.request.LoginRequest;
import com.businessagent.auth.dto.request.RefreshRequest;
import com.businessagent.auth.dto.request.RegisterRequest;
import com.businessagent.auth.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshRequest request);
}
