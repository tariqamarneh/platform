package com.businessagent.channel.dto.internal;

public record AudioPayload(String mediaId, String mimeType, String sha256) implements MessagePayload {}
