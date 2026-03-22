package com.businessagent.auth.service;

import com.businessagent.auth.dto.request.CreateApiKeyRequest;
import com.businessagent.auth.dto.response.ApiKeyCreatedResponse;
import com.businessagent.auth.dto.response.ApiKeyResponse;
import com.businessagent.auth.dto.response.ApiKeyVerifyResponse;

import java.util.List;
import java.util.UUID;

public interface ApiKeyService {

    ApiKeyCreatedResponse createKey(UUID businessId, CreateApiKeyRequest request);

    ApiKeyVerifyResponse verifyKey(String rawKey);

    void revokeKey(UUID keyId, UUID businessId);

    List<ApiKeyResponse> listKeys(UUID businessId);
}
