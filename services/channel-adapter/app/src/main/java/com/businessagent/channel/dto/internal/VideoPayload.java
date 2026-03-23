package com.businessagent.channel.dto.internal;

public record VideoPayload(String mediaId, String mimeType, String caption, String sha256) implements MessagePayload {}
