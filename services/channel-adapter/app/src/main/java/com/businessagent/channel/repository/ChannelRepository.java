package com.businessagent.channel.repository;

import com.businessagent.channel.model.Channel;
import com.businessagent.channel.model.enums.ChannelStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, UUID> {
    boolean existsByBusinessIdAndPhoneNumberId(UUID businessId, String phoneNumberId);
    List<Channel> findByBusinessIdAndStatus(UUID businessId, ChannelStatus status);
    Optional<Channel> findByWebhookToken(String webhookToken);
}
