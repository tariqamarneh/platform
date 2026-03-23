package com.businessagent.channel.model;

import com.businessagent.channel.model.enums.ChannelProvider;
import com.businessagent.channel.model.enums.ChannelStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "channels")
@Getter
@Setter
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChannelProvider provider;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "phone_number_id", nullable = false)
    private String phoneNumberId;

    @Column(name = "waba_id")
    private String wabaId;

    @Column(name = "api_key_encrypted", nullable = false)
    private String apiKeyEncrypted;

    @Column(name = "webhook_token", nullable = false, unique = true)
    private String webhookToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChannelStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
