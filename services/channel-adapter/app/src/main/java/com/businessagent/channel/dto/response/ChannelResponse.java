package com.businessagent.channel.dto.response;

import com.businessagent.channel.model.enums.ChannelProvider;
import com.businessagent.channel.model.enums.ChannelStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChannelResponse(
    UUID id,
    UUID businessId,
    ChannelProvider provider,
    String displayName,
    String phoneNumber,
    String phoneNumberId,
    String wabaId,
    ChannelStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
