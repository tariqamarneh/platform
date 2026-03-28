package com.businessagent.inbox.repository;

import com.businessagent.inbox.model.Conversation;
import com.businessagent.inbox.model.enums.ConversationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    Optional<Conversation> findByContactIdAndChannelIdAndStatus(UUID contactId, UUID channelId, ConversationStatus status);
}
