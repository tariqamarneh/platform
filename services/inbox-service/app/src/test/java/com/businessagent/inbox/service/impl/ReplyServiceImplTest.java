package com.businessagent.inbox.service.impl;

import com.businessagent.inbox.client.ChannelAdapterClient;
import com.businessagent.inbox.dto.request.ReplyRequest;
import com.businessagent.inbox.dto.response.ReplyResult;
import com.businessagent.inbox.exception.ConversationNotFoundException;
import com.businessagent.inbox.model.Contact;
import com.businessagent.inbox.model.Conversation;
import com.businessagent.inbox.model.Message;
import com.businessagent.inbox.model.enums.AssigneeType;
import com.businessagent.inbox.model.enums.ConversationStatus;
import com.businessagent.inbox.model.enums.MessageActorType;
import com.businessagent.inbox.model.enums.MessageContentType;
import com.businessagent.inbox.model.enums.MessageDirection;
import com.businessagent.inbox.model.enums.MessageStatus;
import com.businessagent.inbox.repository.ConversationRepository;
import com.businessagent.inbox.repository.MessageRepository;
import com.businessagent.inbox.service.OutboundQueueService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReplyServiceImplTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private OutboundQueueService outboundQueueService;

    @Mock
    private ChannelAdapterClient channelAdapterClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ReplyServiceImpl replyService;

    private static final UUID BUSINESS_ID = UUID.randomUUID();
    private static final UUID CHANNEL_ID = UUID.randomUUID();
    private static final UUID CONTACT_ID = UUID.randomUUID();
    private static final UUID CONVERSATION_ID = UUID.randomUUID();
    private static final UUID MESSAGE_ID = UUID.randomUUID();

    @Test
    void sendReply_success() {
        ReplyRequest request = buildReplyRequest();
        Conversation conversation = buildConversation();

        when(conversationRepository.findById(CONVERSATION_ID)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> {
            Message msg = inv.getArgument(0);
            msg.setId(MESSAGE_ID);
            return msg;
        });

        ReplyResult result = replyService.sendReply(request);

        assertTrue(result.success());
        assertEquals(MESSAGE_ID.toString(), result.messageId());
        assertNull(result.error());

        verify(messageRepository).save(any(Message.class));
        verify(outboundQueueService).enqueue(eq(CHANNEL_ID.toString()), eq(MESSAGE_ID.toString()));
    }

    @Test
    void sendReply_conversationNotFound() {
        ReplyRequest request = buildReplyRequest();

        when(conversationRepository.findById(CONVERSATION_ID)).thenReturn(Optional.empty());

        assertThrows(ConversationNotFoundException.class,
                () -> replyService.sendReply(request));

        verify(messageRepository, never()).save(any());
        verify(outboundQueueService, never()).enqueue(any(), any());
    }

    @Test
    void sendReply_messageHasCorrectFields() {
        ReplyRequest request = buildReplyRequest();
        Conversation conversation = buildConversation();

        when(conversationRepository.findById(CONVERSATION_ID)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> {
            Message msg = inv.getArgument(0);
            msg.setId(MESSAGE_ID);
            return msg;
        });

        replyService.sendReply(request);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        Message saved = captor.getValue();

        assertEquals(MessageDirection.OUTBOUND, saved.getDirection());
        assertEquals(MessageActorType.AI_BOT, saved.getActorType());
        assertEquals(MessageStatus.PENDING, saved.getStatus());
        assertEquals(CONVERSATION_ID, saved.getConversationId());
    }

    private ReplyRequest buildReplyRequest() {
        return new ReplyRequest(
                CONVERSATION_ID.toString(),
                BUSINESS_ID.toString(),
                "text",
                Map.of("text", "Hello from AI")
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
        message.setDirection(MessageDirection.OUTBOUND);
        message.setActorType(MessageActorType.AI_BOT);
        message.setContentType(MessageContentType.TEXT);
        message.setContent("{\"text\":\"Hello from AI\"}");
        message.setStatus(MessageStatus.PENDING);
        return message;
    }
}
