package com.businessagent.channel.dto.internal;

import com.businessagent.channel.model.enums.MessageType;

public record InboundMessage(
    String messageId,
    String channelId,
    String businessId,
    String from,
    String customerName,
    long timestamp,
    MessageType type,
    MessagePayload payload
) {}
