package com.businessagent.channel.dto.internal;

public record InteractiveReplyPayload(String id, String title, String description) implements MessagePayload {}
