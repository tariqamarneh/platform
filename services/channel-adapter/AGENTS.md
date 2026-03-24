# Channel Adapter Agent Rules

## Service Overview
Multi-channel messaging adapter. Bridges the platform with WhatsApp Business Cloud API and Instagram Messaging API. Receives webhooks, normalizes messages, sends outbound messages via provider APIs. Extensible to Telegram and other channels.

## Supported Channels
- **WhatsApp** — via Meta WhatsApp Business Cloud API (text, image, video, document, audio, location, contacts, reaction, interactive)
- **Instagram** — via Meta Instagram Messaging API (text, image, video, audio, quick replies)

## Project Structure
```
com.businessagent.channel/
├── config/          AppProperties, SecurityConfig, RequestIdFilter, RestClientConfig
├── controller/      WebhookController, MessageController, ChannelController, HealthController
├── dto/
│   ├── internal/    Normalized message format (sealed MessagePayload interface)
│   ├── webhook/     MetaWebhookPayload, InstagramWebhookPayload
│   └── request/     API request/response DTOs
├── model/           Channel entity, enums (ChannelProvider, ChannelStatus, MessageType)
├── repository/      ChannelRepository
├── service/
│   ├── impl/        ChannelServiceImpl, WebhookServiceImpl, MessageServiceImpl
│   └── provider/    MessageProvider interface, ProviderFactory, WhatsAppCloudProvider, InstagramProvider
├── client/          InboxServiceClient, AuthServiceClient
├── security/        WebhookSignatureValidator, ApiKeyAuthFilter
├── exception/       Custom exceptions + GlobalExceptionHandler
├── converter/       ChannelConverter
└── util/            EncryptionUtil, MessageNormalizer, InstagramMessageNormalizer
```

## Key Conventions
- **Provider pattern**: `MessageProvider` interface → one implementation per channel. `ProviderFactory` routes by `ChannelProvider` enum.
- **Separate webhook endpoints**: `/webhook/whatsapp/{token}` and `/webhook/instagram/{token}` — not shared.
- **Provider-specific validation**: `ChannelServiceImpl.createChannel()` validates required fields per provider (phoneNumberId/wabaId for WhatsApp, pageId/instagramAccountId for Instagram).
- **Channel API keys**: AES-256-GCM encrypted at rest. Never returned in API responses. Webhook tokens returned only on creation.
- **Message normalization**: WhatsApp uses `MessageNormalizer`, Instagram uses `InstagramMessageNormalizer`. Both produce `InboundMessage` with `MessagePayload` sealed interface.
- **Webhook security**: HMAC-SHA256 signature validation (same Meta app secret for both channels). Timing-safe comparison.
- **API key auth**: All `/api/v1/**` endpoints require `X-API-Key` header validated against auth-service via `ApiKeyAuthFilter`.
- **Logging**: SLF4J with MDC `requestId`. Never log API keys, webhook tokens, or message content.

## Database
- Single table: `channels` (owned by this service)
- Flyway migrations: V1 (create table), V2 (add Instagram columns, partial unique indexes)
- `ddl-auto: validate` — schema changes must go through Flyway
- Partial unique indexes: `(business_id, phone_number_id) WHERE phone_number_id IS NOT NULL` and `(business_id, instagram_account_id) WHERE instagram_account_id IS NOT NULL`

## Adding a New Channel (e.g., Telegram)
1. Add value to `ChannelProvider` enum
2. Create `TelegramProvider implements MessageProvider`
3. Add case to `ProviderFactory.getProvider()`
4. Create webhook DTO (`TelegramWebhookPayload`)
5. Create normalizer (`TelegramMessageNormalizer`)
6. Add webhook endpoints to `WebhookController`: `GET/POST /webhook/telegram/{token}`
7. Add `processTelegramWebhook()` to `WebhookService`
8. Add Telegram-specific columns via new Flyway migration
9. Add provider-specific validation in `ChannelServiceImpl.createChannel()`

## Service Communication
- **Inbound WhatsApp**: Meta webhook → POST /webhook/whatsapp/{token} → normalize → forward to inbox-service
- **Inbound Instagram**: Meta webhook → POST /webhook/instagram/{token} → normalize → forward to inbox-service
- **Outbound**: inbox-service → POST /api/v1/messages/send → ProviderFactory → Meta API
- **Auth**: ApiKeyAuthFilter → auth-service POST /api/v1/keys/verify
