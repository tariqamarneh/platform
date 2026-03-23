package com.businessagent.channel.service.impl;

import com.businessagent.channel.config.AppProperties;
import com.businessagent.channel.converter.ChannelConverter;
import com.businessagent.channel.dto.request.CreateChannelRequest;
import com.businessagent.channel.dto.request.UpdateChannelRequest;
import com.businessagent.channel.dto.response.ChannelResponse;
import com.businessagent.channel.exception.ChannelNotFoundException;
import com.businessagent.channel.exception.DuplicateChannelException;
import com.businessagent.channel.model.Channel;
import com.businessagent.channel.model.enums.ChannelProvider;
import com.businessagent.channel.model.enums.ChannelStatus;
import com.businessagent.channel.repository.ChannelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelServiceImplTest {

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private ChannelConverter channelConverter;

    @Mock
    private AppProperties appProperties;

    @InjectMocks
    private ChannelServiceImpl channelService;

    private static final UUID CHANNEL_ID = UUID.randomUUID();
    private static final UUID BUSINESS_ID = UUID.randomUUID();

    @Test
    void createChannel_happyPath_shouldSaveWithEncryptedKeyAndGenerateWebhookToken() {
        CreateChannelRequest request = new CreateChannelRequest(
                BUSINESS_ID, "My Channel", "+5511999990000", "phone-id-1", "waba-1", "api-key-123");

        when(channelRepository.existsByBusinessIdAndPhoneNumberId(BUSINESS_ID, "phone-id-1")).thenReturn(false);
        when(appProperties.encryption()).thenReturn(new AppProperties.EncryptionProperties("test-encryption-key-32-bytes-ok!"));
        when(channelRepository.save(any(Channel.class))).thenAnswer(inv -> {
            Channel ch = inv.getArgument(0);
            ch.setId(CHANNEL_ID);
            return ch;
        });
        when(channelConverter.toResponse(any(Channel.class))).thenReturn(buildChannelResponse());

        ChannelResponse response = channelService.createChannel(request);

        assertNotNull(response);
        ArgumentCaptor<Channel> captor = ArgumentCaptor.forClass(Channel.class);
        verify(channelRepository).save(captor.capture());
        Channel saved = captor.getValue();

        assertNotNull(saved.getApiKeyEncrypted());
        assertNotEquals("api-key-123", saved.getApiKeyEncrypted());
        assertNotNull(saved.getWebhookToken());
        assertEquals(ChannelStatus.ACTIVE, saved.getStatus());
        assertEquals(ChannelProvider.WHATSAPP, saved.getProvider());
    }

    @Test
    void createChannel_duplicatePhone_shouldThrowDuplicateChannelException() {
        CreateChannelRequest request = new CreateChannelRequest(
                BUSINESS_ID, "My Channel", "+5511999990000", "phone-id-1", "waba-1", "api-key-123");

        when(channelRepository.existsByBusinessIdAndPhoneNumberId(BUSINESS_ID, "phone-id-1")).thenReturn(true);

        assertThrows(DuplicateChannelException.class, () -> channelService.createChannel(request));
        verify(channelRepository, never()).save(any());
    }

    @Test
    void getChannel_found_shouldReturnResponse() {
        Channel channel = buildChannel();
        when(channelRepository.findById(CHANNEL_ID)).thenReturn(Optional.of(channel));
        when(channelConverter.toResponse(channel)).thenReturn(buildChannelResponse());

        ChannelResponse response = channelService.getChannel(CHANNEL_ID);

        assertNotNull(response);
        assertEquals(CHANNEL_ID, response.id());
    }

    @Test
    void getChannel_notFound_shouldThrowChannelNotFoundException() {
        when(channelRepository.findById(CHANNEL_ID)).thenReturn(Optional.empty());

        assertThrows(ChannelNotFoundException.class, () -> channelService.getChannel(CHANNEL_ID));
    }

    @Test
    void getChannelsByBusiness_shouldReturnList() {
        Channel channel = buildChannel();
        when(channelRepository.findByBusinessIdAndStatus(BUSINESS_ID, ChannelStatus.ACTIVE))
                .thenReturn(List.of(channel));
        when(channelConverter.toResponse(channel)).thenReturn(buildChannelResponse());

        List<ChannelResponse> result = channelService.getChannelsByBusiness(BUSINESS_ID);

        assertEquals(1, result.size());
    }

    @Test
    void updateChannel_shouldUpdateFieldsAndReEncryptIfApiKeyProvided() {
        Channel channel = buildChannel();
        when(channelRepository.findById(CHANNEL_ID)).thenReturn(Optional.of(channel));
        when(appProperties.encryption()).thenReturn(new AppProperties.EncryptionProperties("test-encryption-key-32-bytes-ok!"));
        when(channelRepository.save(any(Channel.class))).thenAnswer(inv -> inv.getArgument(0));
        when(channelConverter.toResponse(any(Channel.class))).thenReturn(buildChannelResponse());

        UpdateChannelRequest request = new UpdateChannelRequest("New Name", "+5511888880000", "new-api-key");
        ChannelResponse response = channelService.updateChannel(CHANNEL_ID, request);

        assertNotNull(response);
        assertEquals("New Name", channel.getDisplayName());
        assertEquals("+5511888880000", channel.getPhoneNumber());
        assertNotNull(channel.getApiKeyEncrypted());
        verify(channelRepository).save(channel);
    }

    @Test
    void deactivateChannel_shouldSetInactiveAndSave() {
        Channel channel = buildChannel();
        when(channelRepository.findById(CHANNEL_ID)).thenReturn(Optional.of(channel));
        when(channelRepository.save(any(Channel.class))).thenAnswer(inv -> inv.getArgument(0));

        channelService.deactivateChannel(CHANNEL_ID);

        assertEquals(ChannelStatus.INACTIVE, channel.getStatus());
        verify(channelRepository).save(channel);
    }

    private Channel buildChannel() {
        Channel channel = new Channel();
        channel.setId(CHANNEL_ID);
        channel.setBusinessId(BUSINESS_ID);
        channel.setProvider(ChannelProvider.WHATSAPP);
        channel.setDisplayName("My Channel");
        channel.setPhoneNumber("+5511999990000");
        channel.setPhoneNumberId("phone-id-1");
        channel.setWabaId("waba-1");
        channel.setApiKeyEncrypted("encrypted-key");
        channel.setWebhookToken("webhook-token");
        channel.setStatus(ChannelStatus.ACTIVE);
        return channel;
    }

    private ChannelResponse buildChannelResponse() {
        return new ChannelResponse(
                CHANNEL_ID, BUSINESS_ID, ChannelProvider.WHATSAPP,
                "My Channel", "+5511999990000", "phone-id-1", "waba-1",
                "webhook-token", ChannelStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now());
    }
}
