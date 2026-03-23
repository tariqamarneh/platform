package com.businessagent.channel.service.impl;

import com.businessagent.channel.config.AppProperties;
import com.businessagent.channel.dto.request.ReadReceiptRequest;
import com.businessagent.channel.dto.request.SendMessageRequest;
import com.businessagent.channel.dto.request.TypingRequest;
import com.businessagent.channel.dto.response.SendMessageResponse;
import com.businessagent.channel.exception.ChannelNotFoundException;
import com.businessagent.channel.model.Channel;
import com.businessagent.channel.repository.ChannelRepository;
import com.businessagent.channel.service.MessageService;
import com.businessagent.channel.service.provider.MessageProvider;
import com.businessagent.channel.service.provider.ProviderFactory;
import com.businessagent.channel.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);
    private final ChannelRepository channelRepository;
    private final ProviderFactory providerFactory;
    private final AppProperties appProperties;

    @Override
    public SendMessageResponse sendMessage(SendMessageRequest request) {
        Channel channel = findChannel(request.channelId());
        String apiKey = EncryptionUtil.decrypt(channel.getApiKeyEncrypted(), appProperties.encryption().key());
        MessageProvider provider = providerFactory.getProvider(channel);
        return provider.sendMessage(channel, apiKey, request);
    }

    @Override
    public void sendTypingIndicator(TypingRequest request) {
        Channel channel = findChannel(request.channelId());
        String apiKey = EncryptionUtil.decrypt(channel.getApiKeyEncrypted(), appProperties.encryption().key());
        MessageProvider provider = providerFactory.getProvider(channel);
        provider.sendTypingIndicator(channel, apiKey, request.to());
    }

    @Override
    public void markAsRead(ReadReceiptRequest request) {
        Channel channel = findChannel(request.channelId());
        String apiKey = EncryptionUtil.decrypt(channel.getApiKeyEncrypted(), appProperties.encryption().key());
        MessageProvider provider = providerFactory.getProvider(channel);
        provider.markAsRead(channel, apiKey, request.messageId());
    }

    private Channel findChannel(String channelId) {
        return channelRepository.findById(UUID.fromString(channelId))
                .orElseThrow(() -> new ChannelNotFoundException("Channel not found: " + channelId));
    }
}
