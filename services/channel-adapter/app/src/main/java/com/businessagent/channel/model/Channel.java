package com.businessagent.channel.model;

import com.businessagent.channel.model.enums.ChannelProvider;
import com.businessagent.channel.model.enums.ChannelStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "channels")
@Getter
@Setter
public class Channel extends BaseEntity {

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChannelProvider provider;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "phone_number_id")
    private String phoneNumberId;

    @Column(name = "waba_id")
    private String wabaId;

    @Column(name = "api_key_encrypted", nullable = false)
    private String apiKeyEncrypted;

    @Column(name = "webhook_token", nullable = false, unique = true)
    private String webhookToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChannelStatus status = ChannelStatus.ACTIVE;

    @Version
    private Long version;
}
