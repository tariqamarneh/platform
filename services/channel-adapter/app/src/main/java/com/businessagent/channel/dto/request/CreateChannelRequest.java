package com.businessagent.channel.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateChannelRequest(
    @NotNull UUID businessId,
    @NotBlank String displayName,
    @NotBlank String phoneNumber,
    @NotBlank String phoneNumberId,
    String wabaId,
    @NotBlank String apiKey
) {}
