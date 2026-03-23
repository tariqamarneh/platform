package com.businessagent.channel.util;

import com.businessagent.channel.dto.internal.*;
import com.businessagent.channel.dto.webhook.MetaWebhookPayload;
import com.businessagent.channel.model.Channel;
import com.businessagent.channel.model.enums.MessageType;

public final class MessageNormalizer {

    private MessageNormalizer() {}

    public static InboundMessage normalize(Channel channel, MetaWebhookPayload.Message msg, MetaWebhookPayload.Value value) {
        String customerName = null;
        if (value.contacts() != null && !value.contacts().isEmpty()) {
            var profile = value.contacts().get(0).profile();
            if (profile != null) customerName = profile.name();
        }

        MessageType type = parseType(msg.type());
        MessagePayload payload = normalizePayload(msg, type);

        return new InboundMessage(
                msg.id(),
                channel.getId().toString(),
                channel.getBusinessId().toString(),
                msg.from(),
                customerName,
                Long.parseLong(msg.timestamp()),
                type,
                payload
        );
    }

    private static MessageType parseType(String type) {
        if (type == null) return MessageType.UNKNOWN;
        return switch (type.toLowerCase()) {
            case "text" -> MessageType.TEXT;
            case "image" -> MessageType.IMAGE;
            case "video" -> MessageType.VIDEO;
            case "document" -> MessageType.DOCUMENT;
            case "audio" -> MessageType.AUDIO;
            case "location" -> MessageType.LOCATION;
            case "contacts" -> MessageType.CONTACTS;
            case "sticker" -> MessageType.STICKER;
            case "reaction" -> MessageType.REACTION;
            case "interactive" -> MessageType.INTERACTIVE;
            default -> MessageType.UNKNOWN;
        };
    }

    private static MessagePayload normalizePayload(MetaWebhookPayload.Message msg, MessageType type) {
        return switch (type) {
            case TEXT -> new TextPayload(msg.text() != null ? msg.text().body() : "");
            case IMAGE -> msg.image() != null
                    ? new ImagePayload(msg.image().id(), msg.image().mimeType(), msg.image().caption(), msg.image().sha256())
                    : new UnknownPayload("image");
            case VIDEO -> msg.video() != null
                    ? new VideoPayload(msg.video().id(), msg.video().mimeType(), msg.video().caption(), msg.video().sha256())
                    : new UnknownPayload("video");
            case DOCUMENT -> msg.document() != null
                    ? new DocumentPayload(msg.document().id(), msg.document().mimeType(), msg.document().caption(), msg.document().filename(), msg.document().sha256())
                    : new UnknownPayload("document");
            case AUDIO -> msg.audio() != null
                    ? new AudioPayload(msg.audio().id(), msg.audio().mimeType(), msg.audio().sha256())
                    : new UnknownPayload("audio");
            case LOCATION -> msg.location() != null
                    ? new LocationPayload(msg.location().latitude(), msg.location().longitude(), msg.location().name(), msg.location().address())
                    : new UnknownPayload("location");
            case CONTACTS -> msg.contacts() != null && !msg.contacts().isEmpty()
                    ? new ContactPayload(
                    msg.contacts().get(0).name() != null ? msg.contacts().get(0).name().formattedName() : "",
                    msg.contacts().get(0).phones() != null && !msg.contacts().get(0).phones().isEmpty() ? msg.contacts().get(0).phones().get(0).phone() : "")
                    : new UnknownPayload("contacts");
            case REACTION -> msg.reaction() != null
                    ? new ReactionPayload(msg.reaction().emoji(), msg.reaction().messageId())
                    : new UnknownPayload("reaction");
            case INTERACTIVE -> normalizeInteractive(msg);
            default -> new UnknownPayload(msg.type());
        };
    }

    private static MessagePayload normalizeInteractive(MetaWebhookPayload.Message msg) {
        if (msg.interactive() == null) return new UnknownPayload("interactive");
        if (msg.interactive().buttonReply() != null) {
            return new InteractiveReplyPayload(
                    msg.interactive().buttonReply().id(),
                    msg.interactive().buttonReply().title(), null);
        }
        if (msg.interactive().listReply() != null) {
            return new InteractiveReplyPayload(
                    msg.interactive().listReply().id(),
                    msg.interactive().listReply().title(),
                    msg.interactive().listReply().description());
        }
        return new UnknownPayload("interactive");
    }
}
