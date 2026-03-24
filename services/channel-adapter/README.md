# Channel Adapter

Multi-channel messaging adapter for the Business Agent platform. Bridges the platform with WhatsApp and Instagram, extensible to other channels.

## Supported Channels

| Channel | Status | Message Types |
|---|---|---|
| **WhatsApp** | Implemented | Text, image, video, document, audio, location, contacts, reaction, interactive |
| **Instagram** | Implemented | Text, image, video, audio, quick replies |

## What it does

- **Webhook ingestion** — Receives and validates Meta webhooks for WhatsApp and Instagram (HMAC-SHA256 signature verification)
- **Message normalization** — Converts provider-specific payloads into a standard internal format
- **Message sending** — Sends messages via WhatsApp Cloud API and Instagram Messaging API
- **Typing indicators** — Shows typing status to customers (WhatsApp + Instagram)
- **Read receipts** — Marks messages as read (WhatsApp)
- **Channel management** — Multi-tenant CRUD for business channel configurations
- **Provider abstraction** — Interface-based design for adding channels (Telegram, etc.)

## Tech Stack

| | |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4, Spring Security |
| Database | PostgreSQL 17, Flyway |
| HTTP Client | Spring RestClient |
| API Docs | Swagger UI (SpringDoc OpenAPI) |
| Build | Gradle 9.4 |

## Quick Start

```bash
# From platform root
docker compose up --build

# Channel adapter: http://localhost:8081
# Swagger UI: http://localhost:8081/swagger-ui/index.html
```

## API Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | /webhook/whatsapp/{token} | Meta verify | WhatsApp webhook verification |
| POST | /webhook/whatsapp/{token} | Meta signature | Receive WhatsApp messages |
| GET | /webhook/instagram/{token} | Meta verify | Instagram webhook verification |
| POST | /webhook/instagram/{token} | Meta signature | Receive Instagram messages |
| POST | /api/v1/messages/send | API Key | Send a message |
| POST | /api/v1/messages/typing | API Key | Typing indicator |
| POST | /api/v1/messages/read | API Key | Read receipt |
| POST | /api/v1/channels | API Key | Register a channel |
| GET | /api/v1/channels/{id} | API Key | Get channel details |
| GET | /api/v1/channels/business/{businessId} | API Key | List business channels |
| PATCH | /api/v1/channels/{id} | API Key | Update channel |
| DELETE | /api/v1/channels/{id} | API Key | Deactivate channel |

## Testing

```bash
cd services/channel-adapter

# Unit tests (51 tests)
./gradlew test

# E2E tests (requires running services)
./scripts/e2e-test.sh
```

## Environment Variables

| Variable | Description |
|---|---|
| DATABASE_URL | PostgreSQL JDBC URL |
| META_APP_SECRET | Meta app secret for webhook signature validation |
| META_VERIFY_TOKEN | Webhook verification handshake token |
| ENCRYPTION_KEY | AES-256 key for encrypting channel API keys (min 16 chars) |
| INBOX_SERVICE_URL | Inbox service URL for forwarding messages |
| AUTH_SERVICE_URL | Auth service URL for API key validation |
