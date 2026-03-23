package com.businessagent.channel.dto.request;

public record UpdateChannelRequest(
    String displayName,
    String phoneNumber,
    String apiKey
) {}
