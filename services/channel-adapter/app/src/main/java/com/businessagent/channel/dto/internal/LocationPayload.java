package com.businessagent.channel.dto.internal;

public record LocationPayload(double latitude, double longitude, String name, String address) implements MessagePayload {}
