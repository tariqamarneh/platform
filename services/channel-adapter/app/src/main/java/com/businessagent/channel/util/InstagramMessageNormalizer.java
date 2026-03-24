package com.businessagent.channel.util;

import com.businessagent.channel.dto.internal.*;
import com.businessagent.channel.dto.webhook.InstagramWebhookPayload;
import com.businessagent.channel.model.Channel;
import com.businessagent.channel.model.enums.MessageType;

public final class InstagramMessageNormalizer {

    private InstagramMessageNormalizer() {}

    public static InboundMessage normalize(Channel channel, InstagramWebhookPayload.Messaging messaging) {
        var msg = messaging.message();

        MessageType type = determineType(msg);
        MessagePayload payload = normalizePayload(msg, type);

        return new InboundMessage(
            msg.mid(),
            channel.getId().toString(),
            channel.getBusinessId().toString(),
            messaging.sender().id(),    // Instagram-scoped ID (IGSID)
            null,                        // Instagram doesn't send name in webhook
            messaging.timestamp(),
            type,
            payload
        );
    }

    private static MessageType determineType(InstagramWebhookPayload.Message msg) {
        if (msg == null) return MessageType.UNKNOWN;

        // Quick reply
        if (msg.quickReply() != null) return MessageType.INTERACTIVE;

        // Attachments
        if (msg.attachments() != null && !msg.attachments().isEmpty()) {
            String attachType = msg.attachments().get(0).type();
            return switch (attachType != null ? attachType.toLowerCase() : "") {
                case "image" -> MessageType.IMAGE;
                case "video" -> MessageType.VIDEO;
                case "audio" -> MessageType.AUDIO;
                case "file" -> MessageType.DOCUMENT;
                default -> MessageType.UNKNOWN;
            };
        }

        // Text
        if (msg.text() != null && !msg.text().isBlank()) return MessageType.TEXT;

        return MessageType.UNKNOWN;
    }

    private static MessagePayload normalizePayload(InstagramWebhookPayload.Message msg, MessageType type) {
        return switch (type) {
            case TEXT -> new TextPayload(msg.text());
            case IMAGE -> {
                var att = msg.attachments().get(0);
                String url = att.payload() != null ? att.payload().url() : null;
                yield new ImagePayload(url, null, null, null);
            }
            case VIDEO -> {
                var att = msg.attachments().get(0);
                String url = att.payload() != null ? att.payload().url() : null;
                yield new VideoPayload(url, null, null, null);
            }
            case AUDIO -> {
                var att = msg.attachments().get(0);
                String url = att.payload() != null ? att.payload().url() : null;
                yield new AudioPayload(url, null, null);
            }
            case DOCUMENT -> {
                var att = msg.attachments().get(0);
                String url = att.payload() != null ? att.payload().url() : null;
                yield new DocumentPayload(url, null, null, null, null);
            }
            case INTERACTIVE -> new InteractiveReplyPayload(
                null, msg.quickReply() != null ? msg.quickReply().payload() : null, null);
            default -> new UnknownPayload(msg.attachments() != null && !msg.attachments().isEmpty()
                ? msg.attachments().get(0).type() : "unknown");
        };
    }
}
