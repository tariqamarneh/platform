package com.businessagent.channel.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MetaWebhookPayload(
    String object,
    List<Entry> entry
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Entry(
        String id,
        List<Change> changes
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Change(
        String field,
        Value value
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Value(
        @JsonProperty("messaging_product") String messagingProduct,
        Metadata metadata,
        List<Contact> contacts,
        List<Message> messages,
        List<Status> statuses
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Metadata(
        @JsonProperty("display_phone_number") String displayPhoneNumber,
        @JsonProperty("phone_number_id") String phoneNumberId
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Contact(
        Profile profile,
        @JsonProperty("wa_id") String waId
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Profile(String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Message(
        String id,
        String from,
        String timestamp,
        String type,
        Text text,
        Image image,
        Video video,
        Document document,
        Audio audio,
        Location location,
        List<MessageContact> contacts,
        Sticker sticker,
        Reaction reaction,
        Interactive interactive
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Text(String body) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Image(
        String id,
        @JsonProperty("mime_type") String mimeType,
        String caption,
        String sha256
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Video(
        String id,
        @JsonProperty("mime_type") String mimeType,
        String caption,
        String sha256
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Document(
        String id,
        @JsonProperty("mime_type") String mimeType,
        String caption,
        String filename,
        String sha256
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Audio(
        String id,
        @JsonProperty("mime_type") String mimeType,
        String sha256
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Location(
        double latitude,
        double longitude,
        String name,
        String address
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MessageContact(
        ContactName name,
        List<Phone> phones
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContactName(
        @JsonProperty("formatted_name") String formattedName,
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name") String lastName
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Phone(
        String phone,
        String type
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Sticker(
        String id,
        @JsonProperty("mime_type") String mimeType,
        String sha256
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Reaction(
        String emoji,
        @JsonProperty("message_id") String messageId
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Interactive(
        String type,
        @JsonProperty("button_reply") ButtonReply buttonReply,
        @JsonProperty("list_reply") ListReply listReply
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ButtonReply(String id, String title) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ListReply(String id, String title, String description) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Status(
        String id,
        String status,
        String timestamp,
        @JsonProperty("recipient_id") String recipientId
    ) {}
}
