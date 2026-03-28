package com.businessagent.inbox.model;

import com.businessagent.inbox.model.enums.MessageActorType;
import com.businessagent.inbox.model.enums.MessageContentType;
import com.businessagent.inbox.model.enums.MessageDirection;
import com.businessagent.inbox.model.enums.MessageStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


@Entity
@Table(name = "messages")
@Getter
@Setter
public class Message extends BaseEntity {

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false)
    private MessageDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false)
    private MessageActorType actorType;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    private MessageContentType contentType;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "provider_message_id")
    private String providerMessageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MessageStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Version
    private Long version;
}
