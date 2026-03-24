package com.businessagent.inbox.repository;

import com.businessagent.inbox.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContactRepository extends JpaRepository<Contact, UUID> {
    Optional<Contact> findByBusinessIdAndExternalIdAndChannelProvider(UUID businessId, String externalId, String channelProvider);
}
