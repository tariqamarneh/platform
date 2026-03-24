package com.businessagent.channel.converter;

import com.businessagent.channel.dto.response.ChannelCreatedResponse;
import com.businessagent.channel.dto.response.ChannelResponse;
import com.businessagent.channel.model.Channel;
import org.springframework.stereotype.Component;

@Component
public class ChannelConverter {

    public ChannelResponse toResponse(Channel channel) {
        return new ChannelResponse(
                channel.getId(),
                channel.getBusinessId(),
                channel.getProvider(),
                channel.getDisplayName(),
                channel.getPhoneNumber(),
                channel.getPhoneNumberId(),
                channel.getWabaId(),
                channel.getStatus(),
                channel.getCreatedAt(),
                channel.getUpdatedAt()
        );
    }

    public ChannelCreatedResponse toCreatedResponse(Channel channel) {
        return new ChannelCreatedResponse(
                channel.getId(),
                channel.getBusinessId(),
                channel.getProvider(),
                channel.getDisplayName(),
                channel.getPhoneNumber(),
                channel.getPhoneNumberId(),
                channel.getWabaId(),
                channel.getWebhookToken(),
                channel.getStatus(),
                channel.getCreatedAt()
        );
    }
}
