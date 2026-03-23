# Channel Adapter

Channel-agnostic messaging adapter for the Business Agent platform. Bridges the platform with WhatsApp Business Cloud API, extensible to Instagram and other channels.

## What it does

- **Webhook ingestion** — Receives and validates Meta WhatsApp webhooks (HMAC-SHA256 signature verification)
- **Message normalization** — Converts provider-specific payloads into a standard internal format
- **Message sending** — Sends text, images, documents, audio, video, location via Meta API
- **Typing indicators** — Shows typing status to customers
- **Read receipts** — Marks messages as read
- **Channel management** — Multi-tenant CRUD for WhatsApp business configurations
- **Provider abstraction** — Interface-based design for adding channels later

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

| Method | Path | Description |
|---|---|---|
| GET | /webhook/{token} | Meta webhook verification |
| POST | /webhook/{token} | Receive inbound messages |
| POST | /api/v1/messages/send | Send a message |
| POST | /api/v1/messages/typing | Typing indicator |
| POST | /api/v1/messages/read | Read receipt |
| POST | /api/v1/channels | Register a channel |
| GET | /api/v1/channels/{id} | Get channel details |
| GET | /api/v1/channels/business/{businessId} | List business channels |
| PATCH | /api/v1/channels/{id} | Update channel |
| DELETE | /api/v1/channels/{id} | Deactivate channel |

## Testing

```bash
cd services/channel-adapter
./gradlew test    # 41 unit tests
```

## Environment Variables

| Variable | Description |
|---|---|
| DATABASE_URL | PostgreSQL JDBC URL |
| META_APP_SECRET | Meta app secret for webhook validation |
| META_VERIFY_TOKEN | Webhook verification handshake token |
| ENCRYPTION_KEY | AES-256 key for encrypting channel API keys |
| INBOX_SERVICE_URL | Inbox service URL for forwarding messages |
| AUTH_SERVICE_URL | Auth service URL for API key validation |
