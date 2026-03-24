package com.businessagent.channel.dto.internal;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "payloadType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TextPayload.class, name = "text"),
    @JsonSubTypes.Type(value = ImagePayload.class, name = "image"),
    @JsonSubTypes.Type(value = VideoPayload.class, name = "video"),
    @JsonSubTypes.Type(value = DocumentPayload.class, name = "document"),
    @JsonSubTypes.Type(value = AudioPayload.class, name = "audio"),
    @JsonSubTypes.Type(value = LocationPayload.class, name = "location"),
    @JsonSubTypes.Type(value = ContactPayload.class, name = "contact"),
    @JsonSubTypes.Type(value = ReactionPayload.class, name = "reaction"),
    @JsonSubTypes.Type(value = InteractiveReplyPayload.class, name = "interactive"),
    @JsonSubTypes.Type(value = UnknownPayload.class, name = "unknown")
})
public sealed interface MessagePayload permits
    TextPayload, ImagePayload, VideoPayload, DocumentPayload,
    AudioPayload, LocationPayload, ContactPayload, ReactionPayload,
    InteractiveReplyPayload, UnknownPayload {}
