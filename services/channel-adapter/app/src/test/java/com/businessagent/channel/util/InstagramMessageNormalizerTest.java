package com.businessagent.channel.util;

import com.businessagent.channel.dto.internal.*;
import com.businessagent.channel.dto.webhook.InstagramWebhookPayload;
import com.businessagent.channel.model.Channel;
import com.businessagent.channel.model.enums.ChannelProvider;
import com.businessagent.channel.model.enums.ChannelStatus;
import com.businessagent.channel.model.enums.MessageType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InstagramMessageNormalizerTest {

    private static final UUID CHANNEL_ID = UUID.randomUUID();
    private static final UUID BUSINESS_ID = UUID.randomUUID();

    @Test
    void normalize_textMessage_shouldReturnTextType() {
        var message = new InstagramWebhookPayload.Message(
                "mid-1", "Hello from Instagram", null, null, null, null);
        var messaging = buildMessaging(message);

        InboundMessage result = InstagramMessageNormalizer.normalize(buildChannel(), messaging);

        assertEquals("mid-1", result.messageId());
        assertEquals(CHANNEL_ID.toString(), result.channelId());
        assertEquals(BUSINESS_ID.toString(), result.businessId());
        assertEquals("sender-123", result.from());
        assertNull(result.customerName());
        assertEquals(MessageType.TEXT, result.type());
        assertInstanceOf(TextPayload.class, result.payload());
        assertEquals("Hello from Instagram", ((TextPayload) result.payload()).body());
    }

    @Test
    void normalize_imageAttachment_shouldReturnImageType() {
        var attachment = new InstagramWebhookPayload.Attachment(
                "image", new InstagramWebhookPayload.Payload("https://example.com/image.jpg"));
        var message = new InstagramWebhookPayload.Message(
                "mid-2", null, null, List.of(attachment), null, null);
        var messaging = buildMessaging(message);

        InboundMessage result = InstagramMessageNormalizer.normalize(buildChannel(), messaging);

        assertEquals(MessageType.IMAGE, result.type());
        assertInstanceOf(ImagePayload.class, result.payload());
        assertEquals("https://example.com/image.jpg", ((ImagePayload) result.payload()).mediaId());
    }

    @Test
    void normalize_quickReply_shouldReturnInteractiveType() {
        var quickReply = new InstagramWebhookPayload.QuickReply("OPTION_A");
        var message = new InstagramWebhookPayload.Message(
                "mid-3", "Option A", null, null, quickReply, null);
        var messaging = buildMessaging(message);

        InboundMessage result = InstagramMessageNormalizer.normalize(buildChannel(), messaging);

        assertEquals(MessageType.INTERACTIVE, result.type());
        assertInstanceOf(InteractiveReplyPayload.class, result.payload());
        assertEquals("OPTION_A", ((InteractiveReplyPayload) result.payload()).title());
    }

    @Test
    void normalize_echoMessage_shouldStillNormalize() {
        // Echo filtering is done in the service layer, not the normalizer.
        // The normalizer should still produce a valid InboundMessage for echo messages.
        var message = new InstagramWebhookPayload.Message(
                "mid-4", "Echo text", true, null, null, null);
        var messaging = buildMessaging(message);

        InboundMessage result = InstagramMessageNormalizer.normalize(buildChannel(), messaging);

        assertNotNull(result);
        assertEquals("mid-4", result.messageId());
        assertEquals(MessageType.TEXT, result.type());
        assertEquals("Echo text", ((TextPayload) result.payload()).body());
    }

    private InstagramWebhookPayload.Messaging buildMessaging(InstagramWebhookPayload.Message message) {
        return new InstagramWebhookPayload.Messaging(
                new InstagramWebhookPayload.Participant("sender-123"),
                new InstagramWebhookPayload.Participant("recipient-456"),
                1700000000L,
                message,
                null,
                null);
    }

    private Channel buildChannel() {
        Channel channel = new Channel();
        channel.setId(CHANNEL_ID);
        channel.setBusinessId(BUSINESS_ID);
        channel.setProvider(ChannelProvider.INSTAGRAM);
        channel.setWebhookToken("token-ig");
        channel.setStatus(ChannelStatus.ACTIVE);
        return channel;
    }
}
