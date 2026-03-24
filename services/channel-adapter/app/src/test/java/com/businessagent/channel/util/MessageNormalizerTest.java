package com.businessagent.channel.util;

import com.businessagent.channel.dto.internal.*;
import com.businessagent.channel.dto.webhook.MetaWebhookPayload;
import com.businessagent.channel.model.Channel;
import com.businessagent.channel.model.enums.ChannelProvider;
import com.businessagent.channel.model.enums.ChannelStatus;
import com.businessagent.channel.model.enums.MessageType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MessageNormalizerTest {

    private static final UUID CHANNEL_ID = UUID.randomUUID();
    private static final UUID BUSINESS_ID = UUID.randomUUID();

    @Test
    void normalize_textMessage_shouldReturnTextPayload() {
        MetaWebhookPayload.Message msg = new MetaWebhookPayload.Message(
                "msg-1", "5511999990000", "1700000000", "text",
                new MetaWebhookPayload.Text("Hello world"),
                null, null, null, null, null, null, null, null, null);

        InboundMessage result = MessageNormalizer.normalize(buildChannel(), msg, buildValue(null));

        assertEquals(MessageType.TEXT, result.type());
        assertInstanceOf(TextPayload.class, result.payload());
        assertEquals("Hello world", ((TextPayload) result.payload()).body());
        assertEquals("msg-1", result.messageId());
        assertEquals("5511999990000", result.from());
    }

    @Test
    void normalize_imageMessage_shouldReturnImagePayload() {
        MetaWebhookPayload.Message msg = new MetaWebhookPayload.Message(
                "msg-2", "5511999990000", "1700000000", "image",
                null,
                new MetaWebhookPayload.Image("media-id", "image/jpeg", "photo caption", "sha256hash"),
                null, null, null, null, null, null, null, null);

        InboundMessage result = MessageNormalizer.normalize(buildChannel(), msg, buildValue(null));

        assertEquals(MessageType.IMAGE, result.type());
        assertInstanceOf(ImagePayload.class, result.payload());
        ImagePayload payload = (ImagePayload) result.payload();
        assertEquals("media-id", payload.mediaId());
        assertEquals("image/jpeg", payload.mimeType());
        assertEquals("photo caption", payload.caption());
    }

    @Test
    void normalize_locationMessage_shouldReturnLocationPayload() {
        MetaWebhookPayload.Message msg = new MetaWebhookPayload.Message(
                "msg-3", "5511999990000", "1700000000", "location",
                null, null, null, null, null,
                new MetaWebhookPayload.Location(-23.55, -46.63, "Office", "123 Main St"),
                null, null, null, null);

        InboundMessage result = MessageNormalizer.normalize(buildChannel(), msg, buildValue(null));

        assertEquals(MessageType.LOCATION, result.type());
        assertInstanceOf(LocationPayload.class, result.payload());
        LocationPayload payload = (LocationPayload) result.payload();
        assertEquals(-23.55, payload.latitude(), 0.001);
        assertEquals(-46.63, payload.longitude(), 0.001);
        assertEquals("Office", payload.name());
    }

    @Test
    void normalize_reactionMessage_shouldReturnReactionPayload() {
        MetaWebhookPayload.Message msg = new MetaWebhookPayload.Message(
                "msg-4", "5511999990000", "1700000000", "reaction",
                null, null, null, null, null, null, null, null,
                new MetaWebhookPayload.Reaction("\uD83D\uDC4D", "original-msg-id"),
                null);

        InboundMessage result = MessageNormalizer.normalize(buildChannel(), msg, buildValue(null));

        assertEquals(MessageType.REACTION, result.type());
        assertInstanceOf(ReactionPayload.class, result.payload());
        ReactionPayload payload = (ReactionPayload) result.payload();
        assertEquals("\uD83D\uDC4D", payload.emoji());
        assertEquals("original-msg-id", payload.messageId());
    }

    @Test
    void normalize_interactiveButtonReply_shouldReturnInteractiveReplyPayload() {
        MetaWebhookPayload.Interactive interactive = new MetaWebhookPayload.Interactive(
                "button_reply",
                new MetaWebhookPayload.ButtonReply("btn-1", "Yes"),
                null);

        MetaWebhookPayload.Message msg = new MetaWebhookPayload.Message(
                "msg-5", "5511999990000", "1700000000", "interactive",
                null, null, null, null, null, null, null, null, null, interactive);

        InboundMessage result = MessageNormalizer.normalize(buildChannel(), msg, buildValue(null));

        assertEquals(MessageType.INTERACTIVE, result.type());
        assertInstanceOf(InteractiveReplyPayload.class, result.payload());
        InteractiveReplyPayload payload = (InteractiveReplyPayload) result.payload();
        assertEquals("btn-1", payload.id());
        assertEquals("Yes", payload.title());
        assertNull(payload.description());
    }

    @Test
    void normalize_unknownType_shouldReturnUnknownPayload() {
        MetaWebhookPayload.Message msg = new MetaWebhookPayload.Message(
                "msg-6", "5511999990000", "1700000000", "ephemeral",
                null, null, null, null, null, null, null, null, null, null);

        InboundMessage result = MessageNormalizer.normalize(buildChannel(), msg, buildValue(null));

        assertEquals(MessageType.UNKNOWN, result.type());
        assertInstanceOf(UnknownPayload.class, result.payload());
        assertEquals("ephemeral", ((UnknownPayload) result.payload()).originalType());
    }

    @Test
    void normalize_shouldExtractCustomerNameFromContacts() {
        List<MetaWebhookPayload.Contact> contacts = List.of(
                new MetaWebhookPayload.Contact(
                        new MetaWebhookPayload.Profile("John Doe"), "5511999990000"));

        MetaWebhookPayload.Message msg = new MetaWebhookPayload.Message(
                "msg-7", "5511999990000", "1700000000", "text",
                new MetaWebhookPayload.Text("hi"),
                null, null, null, null, null, null, null, null, null);

        InboundMessage result = MessageNormalizer.normalize(buildChannel(), msg, buildValue(contacts));

        assertEquals("John Doe", result.customerName());
    }

    private Channel buildChannel() {
        Channel channel = new Channel();
        channel.setId(CHANNEL_ID);
        channel.setBusinessId(BUSINESS_ID);
        channel.setProvider(ChannelProvider.WHATSAPP);
        channel.setStatus(ChannelStatus.ACTIVE);
        return channel;
    }

    private MetaWebhookPayload.Value buildValue(List<MetaWebhookPayload.Contact> contacts) {
        return new MetaWebhookPayload.Value("whatsapp", null, contacts, null, null);
    }
}
