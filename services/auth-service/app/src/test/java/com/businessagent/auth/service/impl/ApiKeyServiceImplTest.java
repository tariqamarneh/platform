package com.businessagent.auth.service.impl;

import com.businessagent.auth.converter.ApiKeyConverter;
import com.businessagent.auth.dto.request.CreateApiKeyRequest;
import com.businessagent.auth.dto.response.ApiKeyCreatedResponse;
import com.businessagent.auth.dto.response.ApiKeyResponse;
import com.businessagent.auth.dto.response.ApiKeyVerifyResponse;
import com.businessagent.auth.exception.ResourceNotFoundException;
import com.businessagent.auth.model.ApiKey;
import com.businessagent.auth.repository.ApiKeyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiKeyServiceImplTest {

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Mock
    private ApiKeyConverter apiKeyConverter;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private ApiKeyServiceImpl apiKeyService;

    private static final UUID BUSINESS_ID = UUID.randomUUID();
    private static final UUID KEY_ID = UUID.randomUUID();

    @Test
    void createKey_shouldGenerateKeyAndSaveEntityAndReturnCreatedResponse() {
        CreateApiKeyRequest request = new CreateApiKeyRequest("My API Key");
        ApiKey savedKey = buildApiKey(BUSINESS_ID, false);
        ApiKeyCreatedResponse expectedResponse = new ApiKeyCreatedResponse(
                savedKey.getId(), "My API Key", "ba_live_abcd", "ba_live_abcdef1234567890", LocalDateTime.now()
        );

        when(apiKeyRepository.save(any(ApiKey.class))).thenReturn(savedKey);
        when(apiKeyConverter.toCreatedResponse(eq(savedKey), anyString())).thenReturn(expectedResponse);

        ApiKeyCreatedResponse response = apiKeyService.createKey(BUSINESS_ID, request);

        assertNotNull(response);
        verify(apiKeyRepository).save(any(ApiKey.class));

        ArgumentCaptor<ApiKey> captor = ArgumentCaptor.forClass(ApiKey.class);
        verify(apiKeyRepository).save(captor.capture());
        ApiKey capturedKey = captor.getValue();
        assertEquals(BUSINESS_ID, capturedKey.getBusinessId());
        assertEquals("My API Key", capturedKey.getName());
        assertNotNull(capturedKey.getKeyHash());
        assertNotNull(capturedKey.getKeyPrefix());
    }

    @Test
    void verifyKey_validKey_shouldReturnValidTrueWithBusinessId() {
        String rawKey = "ba_live_some_test_key_value_here";
        ApiKey apiKey = buildApiKey(BUSINESS_ID, false);

        when(apiKeyRepository.findByKeyHash(anyString())).thenReturn(Optional.of(apiKey));

        ApiKeyVerifyResponse response = apiKeyService.verifyKey(rawKey);

        assertTrue(response.valid());
        assertEquals(BUSINESS_ID, response.businessId());
    }

    @Test
    void verifyKey_notFound_shouldReturnValidFalse() {
        when(apiKeyRepository.findByKeyHash(anyString())).thenReturn(Optional.empty());

        ApiKeyVerifyResponse response = apiKeyService.verifyKey("ba_live_unknown_key");

        assertFalse(response.valid());
        assertNull(response.businessId());
    }

    @Test
    void verifyKey_revokedKey_shouldReturnValidFalse() {
        ApiKey revokedKey = buildApiKey(BUSINESS_ID, true);

        when(apiKeyRepository.findByKeyHash(anyString())).thenReturn(Optional.of(revokedKey));

        ApiKeyVerifyResponse response = apiKeyService.verifyKey("ba_live_revoked_key");

        assertFalse(response.valid());
        assertNull(response.businessId());
    }

    @Test
    void revokeKey_happyPath_shouldSetRevokedTrueAndSave() {
        ApiKey apiKey = buildApiKey(BUSINESS_ID, false);

        when(apiKeyRepository.findById(KEY_ID)).thenReturn(Optional.of(apiKey));
        when(apiKeyRepository.save(any(ApiKey.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cacheManager.getCache("apiKeyVerification")).thenReturn(cache);

        apiKeyService.revokeKey(KEY_ID, BUSINESS_ID);

        ArgumentCaptor<ApiKey> captor = ArgumentCaptor.forClass(ApiKey.class);
        verify(apiKeyRepository).save(captor.capture());
        assertTrue(captor.getValue().isRevoked());
    }

    @Test
    void revokeKey_wrongBusiness_shouldThrowResourceNotFoundException() {
        UUID differentBusinessId = UUID.randomUUID();
        ApiKey apiKey = buildApiKey(BUSINESS_ID, false);

        when(apiKeyRepository.findById(KEY_ID)).thenReturn(Optional.of(apiKey));

        assertThrows(ResourceNotFoundException.class,
                () -> apiKeyService.revokeKey(KEY_ID, differentBusinessId));
    }

    @Test
    void revokeKey_notFound_shouldThrowResourceNotFoundException() {
        when(apiKeyRepository.findById(KEY_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> apiKeyService.revokeKey(KEY_ID, BUSINESS_ID));
    }

    @Test
    void listKeys_shouldReturnConvertedList() {
        ApiKey key1 = buildApiKey(BUSINESS_ID, false);
        ApiKey key2 = buildApiKey(BUSINESS_ID, false);
        key2.setName("Second Key");
        List<ApiKey> keys = List.of(key1, key2);

        ApiKeyResponse resp1 = new ApiKeyResponse(key1.getId(), key1.getName(), key1.getKeyPrefix(), LocalDateTime.now());
        ApiKeyResponse resp2 = new ApiKeyResponse(key2.getId(), key2.getName(), key2.getKeyPrefix(), LocalDateTime.now());

        when(apiKeyRepository.findByBusinessIdAndRevokedFalse(BUSINESS_ID)).thenReturn(keys);
        when(apiKeyConverter.toResponse(key1)).thenReturn(resp1);
        when(apiKeyConverter.toResponse(key2)).thenReturn(resp2);

        List<ApiKeyResponse> result = apiKeyService.listKeys(BUSINESS_ID);

        assertEquals(2, result.size());
        verify(apiKeyRepository).findByBusinessIdAndRevokedFalse(BUSINESS_ID);
    }

    private ApiKey buildApiKey(UUID businessId, boolean revoked) {
        ApiKey apiKey = new ApiKey();
        apiKey.setId(KEY_ID);
        apiKey.setBusinessId(businessId);
        apiKey.setName("Test Key");
        apiKey.setKeyHash("somehashvalue");
        apiKey.setKeyPrefix("ba_live_abcd");
        apiKey.setRevoked(revoked);
        return apiKey;
    }
}
