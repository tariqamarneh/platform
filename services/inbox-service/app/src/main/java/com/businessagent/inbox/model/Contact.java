package com.businessagent.inbox.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "contacts")
@Getter
@Setter
public class Contact extends BaseEntity {

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "channel_provider", nullable = false)
    private String channelProvider;

    @Version
    @Column(name = "version")
    private Long version;
}
