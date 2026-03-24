package com.businessagent.channel.service.impl;

import com.businessagent.channel.config.AppProperties;
import com.businessagent.channel.converter.ChannelConverter;
import com.businessagent.channel.dto.request.CreateChannelRequest;
import com.businessagent.channel.dto.request.UpdateChannelRequest;
import com.businessagent.channel.dto.response.ChannelCreatedResponse;
import com.businessagent.channel.dto.response.ChannelResponse;
import com.businessagent.channel.exception.ChannelNotFoundException;
import com.businessagent.channel.exception.DuplicateChannelException;
import com.businessagent.channel.model.Channel;
import com.businessagent.channel.model.enums.ChannelProvider;
import com.businessagent.channel.model.enums.ChannelStatus;
import com.businessagent.channel.repository.ChannelRepository;
import com.businessagent.channel.service.ChannelService;
import com.businessagent.channel.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChannelServiceImpl implements ChannelService {

    private static final Logger log = LoggerFactory.getLogger(ChannelServiceImpl.class);
    private final ChannelRepository channelRepository;
    private final ChannelConverter channelConverter;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public ChannelCreatedResponse createChannel(CreateChannelRequest request) {
        log.info("Creating channel: businessId={}, provider={}", request.businessId(), request.provider());

        // Validate provider-specific required fields
        if (request.provider() == ChannelProvider.WHATSAPP) {
            if (request.phoneNumberId() == null || request.phoneNumberId().isBlank()) {
                throw new IllegalArgumentException("phoneNumberId is required for WhatsApp channels");
            }
            if (request.wabaId() == null || request.wabaId().isBlank()) {
                throw new IllegalArgumentException("wabaId is required for WhatsApp channels");
            }
        } else if (request.provider() == ChannelProvider.INSTAGRAM) {
            if (request.pageId() == null || request.pageId().isBlank()) {
                throw new IllegalArgumentException("pageId is required for Instagram channels");
            }
            if (request.instagramAccountId() == null || request.instagramAccountId().isBlank()) {
                throw new IllegalArgumentException("instagramAccountId is required for Instagram channels");
            }
        }

        // Provider-specific duplicate checks
        if (request.provider() == ChannelProvider.WHATSAPP) {
            if (request.phoneNumberId() != null &&
                channelRepository.existsByBusinessIdAndPhoneNumberId(request.businessId(), request.phoneNumberId())) {
                throw new DuplicateChannelException("WhatsApp channel already exists for this phone number");
            }
        } else if (request.provider() == ChannelProvider.INSTAGRAM) {
            if (request.instagramAccountId() != null &&
                channelRepository.existsByBusinessIdAndInstagramAccountId(request.businessId(), request.instagramAccountId())) {
                throw new DuplicateChannelException("Instagram channel already exists for this account");
            }
        }

        Channel channel = new Channel();
        channel.setBusinessId(request.businessId());
        channel.setProvider(request.provider());
        channel.setDisplayName(request.displayName());
        channel.setPhoneNumber(request.phoneNumber());
        channel.setPhoneNumberId(request.phoneNumberId());
        channel.setWabaId(request.wabaId());
        channel.setPageId(request.pageId());
        channel.setInstagramAccountId(request.instagramAccountId());
        channel.setApiKeyEncrypted(EncryptionUtil.encrypt(request.apiKey(), appProperties.encryption().key()));
        channel.setWebhookToken(UUID.randomUUID().toString());
        channel.setStatus(ChannelStatus.ACTIVE);

        try {
            channel = channelRepository.save(channel);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateChannelException("Channel already exists");
        }

        log.info("Channel created: channelId={}, businessId={}", channel.getId(), channel.getBusinessId());
        return channelConverter.toCreatedResponse(channel);
    }

    @Override
    @Transactional(readOnly = true)
    public ChannelResponse getChannel(UUID id) {
        return channelConverter.toResponse(findChannelById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChannelResponse> getChannelsByBusiness(UUID businessId) {
        return channelRepository.findByBusinessIdAndStatus(businessId, ChannelStatus.ACTIVE)
                .stream()
                .map(channelConverter::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ChannelResponse updateChannel(UUID id, UpdateChannelRequest request) {
        Channel channel = findChannelById(id);

        if (request.displayName() != null) channel.setDisplayName(request.displayName());
        if (request.phoneNumber() != null) channel.setPhoneNumber(request.phoneNumber());
        if (request.apiKey() != null) {
            channel.setApiKeyEncrypted(EncryptionUtil.encrypt(request.apiKey(), appProperties.encryption().key()));
        }

        channel = channelRepository.save(channel);
        log.info("Channel updated: channelId={}", id);
        return channelConverter.toResponse(channel);
    }

    @Override
    @Transactional
    public void deactivateChannel(UUID id) {
        Channel channel = findChannelById(id);
        channel.setStatus(ChannelStatus.INACTIVE);
        channelRepository.save(channel);
        log.info("Channel deactivated: channelId={}", id);
    }

    private Channel findChannelById(UUID id) {
        return channelRepository.findById(id)
                .orElseThrow(() -> new ChannelNotFoundException("Channel not found: " + id));
    }
}
