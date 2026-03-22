package com.businessagent.auth.controller;

import com.businessagent.auth.dto.response.UserResponse;
import com.businessagent.auth.security.AuthenticatedUser;
import com.businessagent.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile management")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get the current authenticated user's profile")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        UserResponse response = userService.getCurrentUser(authenticatedUser.getUserId());
        return ResponseEntity.ok(response);
    }
}
