package com.businessagent.channel.dto.internal;

public record DocumentPayload(String mediaId, String mimeType, String caption, String filename, String sha256) implements MessagePayload {}
