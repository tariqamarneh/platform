package com.businessagent.inbox.service.impl;

import com.businessagent.inbox.dto.response.ConversationResponse;
import com.businessagent.inbox.dto.response.MessageResponse;
import com.businessagent.inbox.exception.ConversationNotFoundException;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationServiceImplTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ConversationServiceImpl conversationService;

    private static final UUID BUSINESS_ID = UUID.randomUUID();
    private static final UUID CHANNEL_ID = UUID.randomUUID();
    private static final UUID CONTACT_ID = UUID.randomUUID();
    private static final UUID CONVERSATION_ID = UUID.randomUUID();
    private static final UUID MESSAGE_ID = UUID.randomUUID();

    @Test
    void getConversation_found() {
        Conversation conversation = buildConversation();
        when(conversationRepository.findById(CONVERSATION_ID)).thenReturn(Optional.of(conversation));

        ConversationResponse response = conversationService.getConversation(CONVERSATION_ID);

        assertNotNull(response);
        assertEquals(CONVERSATION_ID, response.id());
        assertEquals(BUSINESS_ID, response.businessId());
        assertEquals(CHANNEL_ID, response.channelId());
        assertEquals(CONTACT_ID, response.contactId());
        assertEquals(ConversationStatus.OPEN, response.status());
        assertEquals(AssigneeType.AI_BOT, response.assigneeType());
    }

    @Test
    void getConversation_notFound() {
        when(conversationRepository.findById(CONVERSATION_ID)).thenReturn(Optional.empty());

        assertThrows(ConversationNotFoundException.class,
                () -> conversationService.getConversation(CONVERSATION_ID));
    }

    @Test
    void getMessages_found() {
        Message message = buildMessage();
        Pageable pageable = PageRequest.of(0, 50);
        Page<Message> messagePage = new PageImpl<>(List.of(message), pageable, 1);

        when(conversationRepository.existsById(CONVERSATION_ID)).thenReturn(true);
        when(messageRepository.findByConversationIdOrderByCreatedAtDesc(eq(CONVERSATION_ID), any(Pageable.class)))
                .thenReturn(messagePage);

        Page<MessageResponse> result = conversationService.getConversationMessages(CONVERSATION_ID, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        MessageResponse first = result.getContent().get(0);
        assertEquals(MESSAGE_ID, first.id());
        assertEquals(CONVERSATION_ID, first.conversationId());
        assertEquals(MessageDirection.INBOUND, first.direction());
    }

    @Test
    void getMessages_conversationNotFound() {
        Pageable pageable = PageRequest.of(0, 50);
        when(conversationRepository.existsById(CONVERSATION_ID)).thenReturn(false);

        assertThrows(ConversationNotFoundException.class,
                () -> conversationService.getConversationMessages(CONVERSATION_ID, pageable));
    }

    private Conversation buildConversation() {
        Conversation conversation = new Conversation();
        conversation.setId(CONVERSATION_ID);
        conversation.setBusinessId(BUSINESS_ID);
        conversation.setChannelId(CHANNEL_ID);
        conversation.setContactId(CONTACT_ID);
        conversation.setStatus(ConversationStatus.OPEN);
        conversation.setAssigneeType(AssigneeType.AI_BOT);
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
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
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());
        return message;
    }

}
