package com.businessagent.channel.dto.internal;

public record ReactionPayload(String emoji, String messageId) implements MessagePayload {}
