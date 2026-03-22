package com.businessagent.auth.dto.response;

public record AdminAuthResponse(
        String accessToken,
        long expiresIn
) {}
