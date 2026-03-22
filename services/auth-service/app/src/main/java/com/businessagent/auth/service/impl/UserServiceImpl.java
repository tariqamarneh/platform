package com.businessagent.auth.service.impl;

import com.businessagent.auth.converter.UserConverter;
import com.businessagent.auth.dto.response.UserResponse;
import com.businessagent.auth.exception.ResourceNotFoundException;
import com.businessagent.auth.model.User;
import com.businessagent.auth.repository.UserRepository;
import com.businessagent.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final UserConverter userConverter;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UUID userId) {
        log.debug("Fetching current user: userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found: userId={}", userId);
                    return new ResourceNotFoundException("User not found");
                });
        return userConverter.toResponse(user);
    }
}
