package com.businessagent.channel.dto.internal;

public record ImagePayload(String mediaId, String mimeType, String caption, String sha256) implements MessagePayload {}
