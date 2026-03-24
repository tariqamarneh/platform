CREATE UNIQUE INDEX idx_conversations_contact_channel_open ON conversations(contact_id, channel_id) WHERE status = 'OPEN';
