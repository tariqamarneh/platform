CREATE TABLE contacts (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL,
    external_id VARCHAR(100) NOT NULL,
    display_name VARCHAR(255),
    channel_provider VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_contacts_business_id ON contacts(business_id);
CREATE UNIQUE INDEX idx_contacts_business_external ON contacts(business_id, external_id, channel_provider);
