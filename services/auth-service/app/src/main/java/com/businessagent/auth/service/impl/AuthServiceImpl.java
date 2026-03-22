package com.businessagent.auth.service.impl;

import com.businessagent.auth.config.JwtProperties;
import com.businessagent.auth.dto.request.LoginRequest;
import com.businessagent.auth.dto.request.RefreshRequest;
import com.businessagent.auth.dto.request.RegisterRequest;
import com.businessagent.auth.dto.response.AuthResponse;
import com.businessagent.auth.exception.DuplicateResourceException;
import com.businessagent.auth.exception.InvalidCredentialsException;
import com.businessagent.auth.exception.InvalidTokenException;
import com.businessagent.auth.model.Business;
import com.businessagent.auth.model.RefreshToken;
import com.businessagent.auth.model.User;
import com.businessagent.auth.model.enums.Plan;
import com.businessagent.auth.model.enums.Role;
import com.businessagent.auth.repository.BusinessRepository;
import com.businessagent.auth.repository.RefreshTokenRepository;
import com.businessagent.auth.repository.UserRepository;
import com.businessagent.auth.security.JwtProvider;
import com.businessagent.auth.util.ApiKeyGenerator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements com.businessagent.auth.service.AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registration attempt for email={}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration failed: email already registered email={}", request.email());
            throw new DuplicateResourceException("Email already registered");
        }

        try {
            Business business = new Business();
            business.setName(request.businessName());
            business.setPlan(Plan.FREE);
            business = businessRepository.save(business);

            User user = new User();
            user.setBusinessId(business.getId());
            user.setEmail(request.email());
            user.setPasswordHash(passwordEncoder.encode(request.password()));
            user.setFirstName(request.firstName());
            user.setLastName(request.lastName());
            user.setRole(Role.OWNER);
            user = userRepository.save(user);

            log.info("Registration successful: userId={}, businessId={}, email={}",
                    user.getId(), business.getId(), user.getEmail());
            return generateAuthResponse(user);
        } catch (DataIntegrityViolationException e) {
            log.warn("Registration failed: concurrent duplicate email={}", request.email());
            throw new DuplicateResourceException("Email already registered");
        }
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email={}", request.email());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Login failed: email not found email={}", request.email());
                    return new InvalidCredentialsException("Invalid email or password");
                });

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Login failed: invalid password for userId={}, email={}", user.getId(), user.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        log.info("Login successful: userId={}, businessId={}", user.getId(), user.getBusinessId());
        return generateAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        log.debug("Token refresh attempt");
        String tokenHash = hashRefreshToken(request.refreshToken());

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> {
                    log.warn("Token refresh failed: token not found");
                    return new InvalidTokenException("Invalid refresh token");
                });

        if (storedToken.isRevoked()) {
            log.warn("Token refresh failed: token revoked, tokenId={}, userId={}",
                    storedToken.getId(), storedToken.getUserId());
            throw new InvalidTokenException("Refresh token has been revoked");
        }

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now(Clock.systemUTC()))) {
            log.warn("Token refresh failed: token expired, tokenId={}, userId={}",
                    storedToken.getId(), storedToken.getUserId());
            throw new InvalidTokenException("Refresh token has expired");
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new InvalidTokenException("User not found for token"));

        log.info("Token refresh successful: userId={}, businessId={}", user.getId(), user.getBusinessId());
        return generateAuthResponse(user);
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken();

        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setUserId(user.getId());
        refreshTokenEntity.setTokenHash(hashRefreshToken(refreshToken));
        refreshTokenEntity.setExpiresAt(LocalDateTime.now(Clock.systemUTC())
                .plus(jwtProperties.refreshTokenExpiry()));
        refreshTokenRepository.save(refreshTokenEntity);

        long expiresIn = jwtProperties.accessTokenExpiry().getSeconds();

        return new AuthResponse(accessToken, refreshToken, expiresIn);
    }

    private String hashRefreshToken(String rawToken) {
        return ApiKeyGenerator.hash(rawToken);
    }
}
