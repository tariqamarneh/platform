package com.businessagent.channel.dto.request;

import com.businessagent.channel.model.enums.ChannelProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateChannelRequest(
    @NotNull UUID businessId,
    @NotNull ChannelProvider provider,
    @NotBlank String displayName,
    @NotBlank String apiKey,
    // WhatsApp-specific (required when provider=WHATSAPP)
    String phoneNumber,
    String phoneNumberId,
    String wabaId,
    // Instagram-specific (required when provider=INSTAGRAM)
    String pageId,
    String instagramAccountId
) {}
