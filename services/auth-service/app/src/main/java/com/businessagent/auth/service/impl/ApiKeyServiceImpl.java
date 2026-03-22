package com.businessagent.auth.service.impl;

import com.businessagent.auth.converter.ApiKeyConverter;
import com.businessagent.auth.dto.request.CreateApiKeyRequest;
import com.businessagent.auth.dto.response.ApiKeyCreatedResponse;
import com.businessagent.auth.dto.response.ApiKeyResponse;
import com.businessagent.auth.dto.response.ApiKeyVerifyResponse;
import com.businessagent.auth.exception.ResourceNotFoundException;
import com.businessagent.auth.model.ApiKey;
import com.businessagent.auth.repository.ApiKeyRepository;
import com.businessagent.auth.service.ApiKeyService;
import com.businessagent.auth.util.ApiKeyGenerator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiKeyServiceImpl implements ApiKeyService {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyServiceImpl.class);
    private static final String CACHE_NAME = "apiKeyVerification";

    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyConverter apiKeyConverter;
    private final CacheManager cacheManager;

    @Override
    @Transactional
    public ApiKeyCreatedResponse createKey(UUID businessId, CreateApiKeyRequest request) {
        log.info("Creating API key: businessId={}, name={}", businessId, request.name());
        String rawKey = ApiKeyGenerator.generate();

        ApiKey apiKey = new ApiKey();
        apiKey.setBusinessId(businessId);
        apiKey.setName(request.name());
        apiKey.setKeyHash(ApiKeyGenerator.hash(rawKey));
        apiKey.setKeyPrefix(ApiKeyGenerator.prefix(rawKey));
        apiKey = apiKeyRepository.save(apiKey);

        log.info("API key created: keyId={}, businessId={}, prefix={}",
                apiKey.getId(), businessId, apiKey.getKeyPrefix());
        return apiKeyConverter.toCreatedResponse(apiKey, rawKey);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "apiKeyVerification", key = "T(com.businessagent.auth.util.ApiKeyGenerator).hash(#rawKey)")
    public ApiKeyVerifyResponse verifyKey(String rawKey) {
        String keyHash = ApiKeyGenerator.hash(rawKey);
        Optional<ApiKey> optionalKey = apiKeyRepository.findByKeyHash(keyHash);

        if (optionalKey.isEmpty()) {
            log.debug("API key verification failed: key not found");
            return new ApiKeyVerifyResponse(false, null);
        }

        ApiKey apiKey = optionalKey.get();
        if (apiKey.isRevoked()) {
            log.warn("API key verification failed: key revoked, keyId={}, businessId={}",
                    apiKey.getId(), apiKey.getBusinessId());
            return new ApiKeyVerifyResponse(false, null);
        }

        log.debug("API key verified: keyId={}, businessId={}", apiKey.getId(), apiKey.getBusinessId());
        return new ApiKeyVerifyResponse(true, apiKey.getBusinessId());
    }

    @Override
    @Transactional
    public void revokeKey(UUID keyId, UUID businessId) {
        log.info("Revoking API key: keyId={}, businessId={}", keyId, businessId);
        ApiKey apiKey = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> {
                    log.warn("API key revocation failed: key not found, keyId={}", keyId);
                    return new ResourceNotFoundException("API key not found");
                });

        if (!apiKey.getBusinessId().equals(businessId)) {
            log.warn("API key revocation failed: business mismatch, keyId={}, requestedBy={}, ownedBy={}",
                    keyId, businessId, apiKey.getBusinessId());
            throw new ResourceNotFoundException("API key not found");
        }

        apiKey.setRevoked(true);
        apiKeyRepository.save(apiKey);

        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.evict(apiKey.getKeyHash());
            log.debug("Evicted API key cache for keyId={}", keyId);
        }

        log.info("API key revoked: keyId={}, businessId={}", keyId, businessId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApiKeyResponse> listKeys(UUID businessId) {
        log.debug("Listing API keys for businessId={}", businessId);
        return apiKeyRepository.findByBusinessIdAndRevokedFalse(businessId).stream()
                .map(apiKeyConverter::toResponse)
                .toList();
    }
}
