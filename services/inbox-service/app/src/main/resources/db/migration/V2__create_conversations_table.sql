CREATE TABLE conversations (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL,
    channel_id UUID NOT NULL,
    contact_id UUID NOT NULL REFERENCES contacts(id),
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    assignee_type VARCHAR(50) NOT NULL DEFAULT 'AI_BOT',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_conversations_business_id ON conversations(business_id);
CREATE INDEX idx_conversations_contact_id ON conversations(contact_id);
CREATE INDEX idx_conversations_channel_id ON conversations(channel_id);
CREATE INDEX idx_conversations_status ON conversations(status);
