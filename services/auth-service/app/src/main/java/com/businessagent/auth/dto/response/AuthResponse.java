package com.businessagent.auth.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresIn
) {
}
