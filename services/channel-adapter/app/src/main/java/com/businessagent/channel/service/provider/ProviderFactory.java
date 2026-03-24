package com.businessagent.channel.service.provider;

import com.businessagent.channel.model.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProviderFactory {
    private final WhatsAppCloudProvider whatsAppCloudProvider;

    public MessageProvider getProvider(Channel channel) {
        return switch (channel.getProvider()) {
            case WHATSAPP -> whatsAppCloudProvider;
        };
    }
}
