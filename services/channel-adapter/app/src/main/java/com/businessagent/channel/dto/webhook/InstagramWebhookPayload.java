package com.businessagent.channel.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InstagramWebhookPayload(
    String object,
    List<Entry> entry
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Entry(
        String id,       // Page ID
        long time,
        List<Messaging> messaging
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Messaging(
        Participant sender,
        Participant recipient,
        long timestamp,
        Message message,
        Postback postback,
        Read read
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Participant(String id) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Message(
        String mid,
        String text,
        @JsonProperty("is_echo") Boolean isEcho,
        List<Attachment> attachments,
        @JsonProperty("quick_reply") QuickReply quickReply,
        @JsonProperty("reply_to") ReplyTo replyTo
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Attachment(
        String type,    // image, video, audio, file, share, story_mention
        Payload payload
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(String url) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record QuickReply(String payload) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReplyTo(
        String mid,
        Story story
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Story(String url, String id) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Postback(
        String mid,
        String title,
        String payload
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Read(long watermark) {}
}
