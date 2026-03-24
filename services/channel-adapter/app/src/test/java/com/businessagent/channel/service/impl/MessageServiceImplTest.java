package com.businessagent.channel.service.impl;

import com.businessagent.channel.config.AppProperties;
import com.businessagent.channel.dto.request.ReadReceiptRequest;
import com.businessagent.channel.dto.request.SendMessageRequest;
import com.businessagent.channel.dto.request.TypingRequest;
import com.businessagent.channel.dto.response.SendMessageResponse;
import com.businessagent.channel.exception.ChannelNotFoundException;
import com.businessagent.channel.model.Channel;
import com.businessagent.channel.model.enums.ChannelProvider;
import com.businessagent.channel.model.enums.ChannelStatus;
import com.businessagent.channel.repository.ChannelRepository;
import com.businessagent.channel.service.provider.MessageProvider;
import com.businessagent.channel.service.provider.ProviderFactory;
import com.businessagent.channel.util.EncryptionUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private ProviderFactory providerFactory;

    @Mock
    private AppProperties appProperties;

    @Mock
    private MessageProvider messageProvider;

    @InjectMocks
    private MessageServiceImpl messageService;

    private static final UUID CHANNEL_ID = UUID.randomUUID();
    private static final UUID BUSINESS_ID = UUID.randomUUID();
    private static final String ENCRYPTION_KEY = "test-encryption-key-32-bytes-ok!";

    private AppProperties buildAppProperties() {
        return new AppProperties(
                new AppProperties.MetaProperties("test-secret", "test-verify-token", "v21.0", "https://graph.facebook.com"),
                new AppProperties.EncryptionProperties(ENCRYPTION_KEY),
                new AppProperties.ServicesProperties("http://localhost:8082", "http://localhost:8080")
        );
    }

    @Test
    void sendMessage_shouldFindChannelDecryptKeyAndDelegateToProvider() {
        Channel channel = buildChannel();
        when(channelRepository.findById(CHANNEL_ID)).thenReturn(Optional.of(channel));
        when(appProperties.encryption()).thenReturn(buildAppProperties().encryption());
        when(providerFactory.getProvider(channel)).thenReturn(messageProvider);

        SendMessageResponse expectedResponse = new SendMessageResponse(true, "wamid.123", null);
        when(messageProvider.sendMessage(eq(channel), anyString(), any(SendMessageRequest.class)))
                .thenReturn(expectedResponse);

        SendMessageRequest request = new SendMessageRequest(CHANNEL_ID.toString(), "5511999990000", "text", Map.of("body", "Hello"));
        SendMessageResponse response = messageService.sendMessage(request);

        assertTrue(response.success());
        assertEquals("wamid.123", response.messageId());
        verify(messageProvider).sendMessage(eq(channel), anyString(), eq(request));
    }

    @Test
    void sendMessage_channelNotFound_shouldThrow() {
        when(channelRepository.findById(CHANNEL_ID)).thenReturn(Optional.empty());

        SendMessageRequest request = new SendMessageRequest(CHANNEL_ID.toString(), "5511999990000", "text", Map.of("body", "Hello"));

        assertThrows(ChannelNotFoundException.class, () -> messageService.sendMessage(request));
    }

    @Test
    void sendTypingIndicator_shouldDelegateToProvider() {
        Channel channel = buildChannel();
        when(channelRepository.findById(CHANNEL_ID)).thenReturn(Optional.of(channel));
        when(appProperties.encryption()).thenReturn(buildAppProperties().encryption());
        when(providerFactory.getProvider(channel)).thenReturn(messageProvider);

        TypingRequest request = new TypingRequest(CHANNEL_ID.toString(), "5511999990000");
        messageService.sendTypingIndicator(request);

        verify(messageProvider).sendTypingIndicator(eq(channel), anyString(), eq("5511999990000"));
    }

    @Test
    void markAsRead_shouldDelegateToProvider() {
        Channel channel = buildChannel();
        when(channelRepository.findById(CHANNEL_ID)).thenReturn(Optional.of(channel));
        when(appProperties.encryption()).thenReturn(buildAppProperties().encryption());
        when(providerFactory.getProvider(channel)).thenReturn(messageProvider);

        ReadReceiptRequest request = new ReadReceiptRequest(CHANNEL_ID.toString(), "wamid.456");
        messageService.markAsRead(request);

        verify(messageProvider).markAsRead(eq(channel), anyString(), eq("wamid.456"));
    }

    private Channel buildChannel() {
        Channel channel = new Channel();
        channel.setId(CHANNEL_ID);
        channel.setBusinessId(BUSINESS_ID);
        channel.setProvider(ChannelProvider.WHATSAPP);
        channel.setPhoneNumberId("phone-id-1");
        channel.setApiKeyEncrypted(EncryptionUtil.encrypt("real-api-key", ENCRYPTION_KEY));
        channel.setWebhookToken("token-abc");
        channel.setStatus(ChannelStatus.ACTIVE);
        return channel;
    }
}
