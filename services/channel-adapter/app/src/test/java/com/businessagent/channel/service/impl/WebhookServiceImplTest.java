package com.businessagent.channel.service.impl;

import com.businessagent.channel.client.InboxServiceClient;
import com.businessagent.channel.config.AppProperties;
import com.businessagent.channel.dto.internal.InboundMessage;
import com.businessagent.channel.dto.webhook.MetaWebhookPayload;
import com.businessagent.channel.exception.WebhookValidationException;
import com.businessagent.channel.model.Channel;
import com.businessagent.channel.model.enums.ChannelProvider;
import com.businessagent.channel.model.enums.ChannelStatus;
import com.businessagent.channel.repository.ChannelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookServiceImplTest {

    @Mock
    private AppProperties appProperties;

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private InboxServiceClient inboxServiceClient;

    @InjectMocks
    private WebhookServiceImpl webhookService;

    private static final UUID CHANNEL_ID = UUID.randomUUID();
    private static final UUID BUSINESS_ID = UUID.randomUUID();

    private AppProperties buildAppProperties() {
        return new AppProperties(
                new AppProperties.MetaProperties("test-secret", "test-verify-token", "v21.0", "https://graph.facebook.com"),
                new AppProperties.EncryptionProperties("test-encryption-key-32-bytes-ok!"),
                new AppProperties.ServicesProperties("http://localhost:8082", "http://localhost:8080")
        );
    }

    @Test
    void verifyWebhook_valid_shouldReturnChallenge() {
        Channel channel = buildChannel();
        when(channelRepository.findByWebhookToken("token-abc")).thenReturn(Optional.of(channel));
        when(appProperties.meta()).thenReturn(buildAppProperties().meta());

        String result = webhookService.verifyWebhook("token-abc", "subscribe", "challenge-123", "test-verify-token");

        assertEquals("challenge-123", result);
    }

    @Test
    void verifyWebhook_unknownToken_shouldThrow() {
        when(channelRepository.findByWebhookToken("unknown")).thenReturn(Optional.empty());

        assertThrows(WebhookValidationException.class,
                () -> webhookService.verifyWebhook("unknown", "subscribe", "challenge", "test-verify-token"));
    }

    @Test
    void verifyWebhook_wrongMode_shouldThrow() {
        Channel channel = buildChannel();
        when(channelRepository.findByWebhookToken("token-abc")).thenReturn(Optional.of(channel));

        assertThrows(WebhookValidationException.class,
                () -> webhookService.verifyWebhook("token-abc", "unsubscribe", "challenge", "test-verify-token"));
    }

    @Test
    void verifyWebhook_wrongVerifyToken_shouldThrow() {
        Channel channel = buildChannel();
        when(channelRepository.findByWebhookToken("token-abc")).thenReturn(Optional.of(channel));
        when(appProperties.meta()).thenReturn(buildAppProperties().meta());

        assertThrows(WebhookValidationException.class,
                () -> webhookService.verifyWebhook("token-abc", "subscribe", "challenge", "wrong-token"));
    }

    @Test
    void processWebhook_textMessage_shouldNormalizeAndForwardToInbox() {
        Channel channel = buildChannel();
        when(channelRepository.findByWebhookToken("token-abc")).thenReturn(Optional.of(channel));

        MetaWebhookPayload.Message message = new MetaWebhookPayload.Message(
                "msg-1", "5511999990000", "1700000000", "text",
                new MetaWebhookPayload.Text("Hello"),
                null, null, null, null, null, null, null, null, null);

        MetaWebhookPayload.Value value = new MetaWebhookPayload.Value(
                "whatsapp", null,
                List.of(new MetaWebhookPayload.Contact(new MetaWebhookPayload.Profile("John"), "5511999990000")),
                List.of(message), null);

        MetaWebhookPayload payload = new MetaWebhookPayload("whatsapp_business_account",
                List.of(new MetaWebhookPayload.Entry("entry-1",
                        List.of(new MetaWebhookPayload.Change("messages", value)))));

        webhookService.processWebhook("token-abc", payload);

        verify(inboxServiceClient).forwardMessage(any(InboundMessage.class));
    }

    private Channel buildChannel() {
        Channel channel = new Channel();
        channel.setId(CHANNEL_ID);
        channel.setBusinessId(BUSINESS_ID);
        channel.setProvider(ChannelProvider.WHATSAPP);
        channel.setWebhookToken("token-abc");
        channel.setStatus(ChannelStatus.ACTIVE);
        return channel;
    }
}
