-- Make WhatsApp-specific columns nullable (Instagram channels don't have them)
ALTER TABLE channels ALTER COLUMN phone_number_id DROP NOT NULL;
ALTER TABLE channels ALTER COLUMN waba_id DROP NOT NULL;

-- Drop the unique index that requires phone_number_id (it's WhatsApp-specific)
DROP INDEX IF EXISTS idx_channels_business_phone;

-- Add Instagram-specific columns
ALTER TABLE channels ADD COLUMN page_id VARCHAR(100);
ALTER TABLE channels ADD COLUMN instagram_account_id VARCHAR(100);

-- New unique indexes per provider
CREATE UNIQUE INDEX idx_channels_business_phone ON channels(business_id, phone_number_id) WHERE phone_number_id IS NOT NULL;
CREATE UNIQUE INDEX idx_channels_business_instagram ON channels(business_id, instagram_account_id) WHERE instagram_account_id IS NOT NULL;
