CREATE TABLE channels (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL,
    provider VARCHAR(50) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50),
    phone_number_id VARCHAR(100) NOT NULL,
    waba_id VARCHAR(100) NOT NULL,
    api_key_encrypted TEXT NOT NULL,
    webhook_token VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_channels_business_id ON channels(business_id);
CREATE UNIQUE INDEX idx_channels_webhook_token ON channels(webhook_token);
CREATE INDEX idx_channels_phone_number_id ON channels(phone_number_id);
CREATE UNIQUE INDEX idx_channels_business_phone ON channels(business_id, phone_number_id);
