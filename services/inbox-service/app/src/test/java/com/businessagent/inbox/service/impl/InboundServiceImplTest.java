package com.businessagent.inbox.service.impl;

import com.businessagent.inbox.client.AiEngineClient;
import com.businessagent.inbox.client.ChannelAdapterClient;
import com.businessagent.inbox.dto.request.InboundMessageRequest;
import com.businessagent.inbox.dto.response.InboundResult;
import com.businessagent.inbox.model.Contact;
import com.businessagent.inbox.model.Conversation;
import com.businessagent.inbox.model.Message;
import com.businessagent.inbox.model.enums.AssigneeType;
import com.businessagent.inbox.model.enums.ConversationStatus;
import com.businessagent.inbox.model.enums.MessageActorType;
import com.businessagent.inbox.model.enums.MessageContentType;
import com.businessagent.inbox.model.enums.MessageDirection;
import com.businessagent.inbox.model.enums.MessageStatus;
import com.businessagent.inbox.repository.ContactRepository;
import com.businessagent.inbox.repository.ConversationRepository;
import com.businessagent.inbox.repository.MessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InboundServiceImplTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private AiEngineClient aiEngineClient;

    @Mock
    private ChannelAdapterClient channelAdapterClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private InboundServiceImpl inboundService;

    private static final UUID BUSINESS_ID = UUID.randomUUID();
    private static final UUID CHANNEL_ID = UUID.randomUUID();
    private static final UUID CONTACT_ID = UUID.randomUUID();
    private static final UUID CONVERSATION_ID = UUID.randomUUID();
    private static final UUID MESSAGE_ID = UUID.randomUUID();
    private static final String API_KEY = "test-api-key";

    @Test
    void processInbound_newContact_createsContactAndConversation() {
        InboundMessageRequest request = buildInboundRequest();

        when(contactRepository.findByBusinessIdAndExternalIdAndChannelProvider(
                eq(BUSINESS_ID), eq("5511999990000"), eq("WHATSAPP")))
                .thenReturn(Optional.empty());
        when(contactRepository.save(any(Contact.class))).thenAnswer(inv -> {
            Contact c = inv.getArgument(0);
            c.setId(CONTACT_ID);
            return c;
        });
        when(conversationRepository.findByContactIdAndChannelIdAndStatus(
                eq(CONTACT_ID), eq(CHANNEL_ID), eq(ConversationStatus.OPEN)))
                .thenReturn(Optional.empty());
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(inv -> {
            Conversation conv = inv.getArgument(0);
            conv.setId(CONVERSATION_ID);
            return conv;
        });
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> {
            Message msg = inv.getArgument(0);
            msg.setId(MESSAGE_ID);
            return msg;
        });

        InboundResult result = inboundService.processInbound(request, API_KEY);

        assertTrue(result.success());
        assertEquals(CONVERSATION_ID.toString(), result.conversationId());
        assertEquals(MESSAGE_ID.toString(), result.messageId());
        assertNull(result.error());

        verify(contactRepository).save(any(Contact.class));
        verify(conversationRepository).save(any(Conversation.class));
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void processInbound_existingContact_reusesContact() {
        InboundMessageRequest request = buildInboundRequest();
        Contact existingContact = buildContact();

        when(contactRepository.findByBusinessIdAndExternalIdAndChannelProvider(
                eq(BUSINESS_ID), eq("5511999990000"), eq("WHATSAPP")))
                .thenReturn(Optional.of(existingContact));
        when(conversationRepository.findByContactIdAndChannelIdAndStatus(
                eq(CONTACT_ID), eq(CHANNEL_ID), eq(ConversationStatus.OPEN)))
                .thenReturn(Optional.empty());
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(inv -> {
            Conversation conv = inv.getArgument(0);
            conv.setId(CONVERSATION_ID);
            return conv;
        });
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> {
            Message msg = inv.getArgument(0);
            msg.setId(MESSAGE_ID);
            return msg;
        });

        InboundResult result = inboundService.processInbound(request, API_KEY);

        assertTrue(result.success());
        verify(contactRepository, never()).save(any(Contact.class));
    }

    @Test
    void processInbound_existingOpenConversation_reusesConversation() {
        InboundMessageRequest request = buildInboundRequest();
        Contact existingContact = buildContact();
        Conversation existingConversation = buildConversation();

        when(contactRepository.findByBusinessIdAndExternalIdAndChannelProvider(
                eq(BUSINESS_ID), eq("5511999990000"), eq("WHATSAPP")))
                .thenReturn(Optional.of(existingContact));
        when(conversationRepository.findByContactIdAndChannelIdAndStatus(
                eq(CONTACT_ID), eq(CHANNEL_ID), eq(ConversationStatus.OPEN)))
                .thenReturn(Optional.of(existingConversation));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> {
            Message msg = inv.getArgument(0);
            msg.setId(MESSAGE_ID);
            return msg;
        });

        InboundResult result = inboundService.processInbound(request, API_KEY);

        assertTrue(result.success());
        verify(conversationRepository, never()).save(any(Conversation.class));
    }

    @Test
    void processInbound_savesMessageWithCorrectFields() {
        InboundMessageRequest request = buildInboundRequest();
        Contact existingContact = buildContact();
        Conversation existingConversation = buildConversation();

        when(contactRepository.findByBusinessIdAndExternalIdAndChannelProvider(
                eq(BUSINESS_ID), eq("5511999990000"), eq("WHATSAPP")))
                .thenReturn(Optional.of(existingContact));
        when(conversationRepository.findByContactIdAndChannelIdAndStatus(
                eq(CONTACT_ID), eq(CHANNEL_ID), eq(ConversationStatus.OPEN)))
                .thenReturn(Optional.of(existingConversation));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> {
            Message msg = inv.getArgument(0);
            msg.setId(MESSAGE_ID);
            return msg;
        });

        inboundService.processInbound(request, API_KEY);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        Message saved = captor.getValue();

        assertEquals(MessageDirection.INBOUND, saved.getDirection());
        assertEquals(MessageActorType.CONTACT, saved.getActorType());
        assertEquals(MessageStatus.DELIVERED, saved.getStatus());
        assertEquals(CONVERSATION_ID, saved.getConversationId());
        assertEquals("wamid.test123", saved.getProviderMessageId());
    }

    private InboundMessageRequest buildInboundRequest() {
        return new InboundMessageRequest(
                "wamid.test123",
                CHANNEL_ID.toString(),
                BUSINESS_ID.toString(),
                "5511999990000",
                "Test Customer",
                System.currentTimeMillis(),
                "text",
                Map.of("text", "Hello")
        );
    }

    private Contact buildContact() {
        Contact contact = new Contact();
        contact.setId(CONTACT_ID);
        contact.setBusinessId(BUSINESS_ID);
        contact.setExternalId("5511999990000");
        contact.setDisplayName("Test Customer");
        contact.setChannelProvider("WHATSAPP");
        return contact;
    }

    private Conversation buildConversation() {
        Conversation conversation = new Conversation();
        conversation.setId(CONVERSATION_ID);
        conversation.setBusinessId(BUSINESS_ID);
        conversation.setChannelId(CHANNEL_ID);
        conversation.setContactId(CONTACT_ID);
        conversation.setStatus(ConversationStatus.OPEN);
        conversation.setAssigneeType(AssigneeType.AI_BOT);
        return conversation;
    }

    private Message buildMessage() {
        Message message = new Message();
        message.setId(MESSAGE_ID);
        message.setConversationId(CONVERSATION_ID);
        message.setDirection(MessageDirection.INBOUND);
        message.setActorType(MessageActorType.CONTACT);
        message.setContentType(MessageContentType.TEXT);
        message.setContent("{\"text\":\"Hello\"}");
        message.setProviderMessageId("wamid.test123");
        message.setStatus(MessageStatus.DELIVERED);
        return message;
    }
}
