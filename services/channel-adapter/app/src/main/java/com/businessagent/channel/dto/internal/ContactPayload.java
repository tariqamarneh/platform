package com.businessagent.channel.dto.internal;

public record ContactPayload(String formattedName, String phone) implements MessagePayload {}
