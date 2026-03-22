package com.businessagent.auth.controller;

import com.businessagent.auth.dto.request.CreateApiKeyRequest;
import com.businessagent.auth.dto.response.ApiKeyCreatedResponse;
import com.businessagent.auth.dto.response.ApiKeyResponse;
import com.businessagent.auth.dto.response.ApiKeyVerifyResponse;
import com.businessagent.auth.security.AuthenticatedUser;
import com.businessagent.auth.service.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/keys")
@RequiredArgsConstructor
@Tag(name = "API Keys", description = "API key management for service-to-service authentication")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping
    @Operation(summary = "Create a new API key for the business")
    public ResponseEntity<ApiKeyCreatedResponse> createKey(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody CreateApiKeyRequest request) {
        ApiKeyCreatedResponse response = apiKeyService.createKey(authenticatedUser.getBusinessId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify an API key (used by ai-engine)")
    @SecurityRequirements
    public ResponseEntity<ApiKeyVerifyResponse> verifyKey(
            @RequestHeader("X-API-Key") String key) {
        ApiKeyVerifyResponse response = apiKeyService.verifyKey(key);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List all active API keys for the business")
    public ResponseEntity<List<ApiKeyResponse>> listKeys(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        List<ApiKeyResponse> response = apiKeyService.listKeys(authenticatedUser.getBusinessId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Revoke an API key")
    public ResponseEntity<Void> revokeKey(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable UUID id) {
        apiKeyService.revokeKey(id, authenticatedUser.getBusinessId());
        return ResponseEntity.noContent().build();
    }
}
