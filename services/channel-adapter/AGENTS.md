# Channel Adapter Agent Rules

## Service Overview
Channel-agnostic messaging adapter. Bridges the platform with WhatsApp Business Cloud API (extensible to Instagram, Telegram). Receives webhooks, normalizes messages, sends outbound messages via provider APIs.

## Project Structure
```
com.businessagent.channel/
├── config/          AppProperties, SecurityConfig, RequestIdFilter, RestClientConfig
├── controller/      WebhookController, MessageController, ChannelController, HealthController
├── dto/
│   ├── internal/    Normalized message format (sealed MessagePayload interface)
│   ├── webhook/     Meta-specific webhook payload DTOs
│   └── request/     API request/response DTOs
├── model/           Channel entity, enums (ChannelProvider, ChannelStatus, MessageType)
├── repository/      ChannelRepository
├── service/
│   ├── impl/        ChannelServiceImpl, WebhookServiceImpl, MessageServiceImpl
│   └── provider/    MessageProvider interface, ProviderFactory, WhatsAppCloudProvider
├── client/          InboxServiceClient, AuthServiceClient
├── security/        WebhookSignatureValidator (HMAC-SHA256)
├── exception/       Custom exceptions + GlobalExceptionHandler
├── converter/       ChannelConverter
└── util/            EncryptionUtil (AES-256-GCM), MessageNormalizer
```

## Key Conventions
- **Provider pattern**: `MessageProvider` interface → implementations per channel. Adding a channel = new implementation + enum value.
- **Channel API keys**: AES-256-GCM encrypted at rest. Never returned in API responses.
- **Webhook tokens**: Unique per channel. Used as path parameter for webhook URLs (`/webhook/{token}`).
- **Message normalization**: All provider-specific payloads are converted to `InboundMessage` with `MessagePayload` sealed interface.
- **Webhook security**: HMAC-SHA256 signature validation using Meta app secret. Timing-safe comparison via `MessageDigest.isEqual`.
- **Logging**: SLF4J with MDC `requestId`. Never log API keys or message content.

## Database
- Single table: `channels` (owned by this service)
- Flyway migrations in `app/src/main/resources/db/migration/`
- `ddl-auto: validate` — schema changes must go through Flyway

## Adding a New Channel (e.g., Instagram)
1. Add value to `ChannelProvider` enum
2. Create `InstagramProvider implements MessageProvider`
3. Add case to `ProviderFactory.getProvider()`
4. Create new webhook DTOs if needed
5. Add normalization logic to `MessageNormalizer`
6. The webhook URL pattern remains the same (`/webhook/{token}`)

## Service Communication
- **Inbound**: Meta webhook → channel-adapter → inbox-service (HTTP POST)
- **Outbound**: inbox-service → channel-adapter → Meta API (HTTP)
- **Auth**: channel-adapter → auth-service (API key validation)
