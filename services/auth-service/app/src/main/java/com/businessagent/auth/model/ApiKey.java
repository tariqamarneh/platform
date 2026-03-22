package com.businessagent.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "api_keys")
@Getter
@Setter
@NoArgsConstructor
public class ApiKey extends BaseEntityNoUpdate {

    @Column(nullable = false)
    private UUID businessId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String keyHash;

    @Column(nullable = false)
    private String keyPrefix;

    @Column(nullable = false)
    private boolean revoked = false;

    @Version
    private Long version;
}
