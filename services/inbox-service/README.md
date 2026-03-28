# Inbox Service

Conversation orchestrator for the Business Agent platform. Manages the full message lifecycle — from inbound message reception to AI processing and outbound delivery.

## What it does

- **Inbound processing** — Receives normalized messages from channel-adapter, creates/reuses conversations and contacts
- **AI integration** — Notifies ai-engine of new messages, provides conversation history, processes AI replies
- **Outbound delivery** — Queues outbound messages in Redis, worker polls and sends via channel-adapter
- **Conversation management** — Get conversation details and paginated message history
- **Retry logic** — Failed outbound messages are re-queued with configurable max retries

## Tech Stack

| | |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4, Spring Security |
| Database | PostgreSQL 17, Flyway |
| Queue | Redis 7 (outbound message queues) |
| API Docs | Swagger UI (SpringDoc OpenAPI) |
| Build | Gradle 9.4 |

## Quick Start

```bash
# From platform root
docker compose up --build

# Inbox service: http://localhost:8082
# Swagger UI: http://localhost:8082/swagger-ui/index.html
```

## API Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | /api/v1/inbound | API Key | Receive message from channel-adapter |
| GET | /api/v1/conversations/{id} | API Key | Get conversation details |
| GET | /api/v1/conversations/{id}/messages | API Key | Get message history (paginated) |
| POST | /api/v1/reply | API Key | Send AI response |
| POST | /api/v1/typing | API Key | Set typing indicator |
| GET | /health | None | Health check |

## Message Flow

```
channel-adapter → POST /api/v1/inbound
    → save contact + conversation + message
    → async: mark as read + typing indicator (via channel-adapter)
    → async: notify ai-engine POST /api/v1/process

ai-engine:
    → GET /api/v1/conversations/{id}/messages (history)
    → POST /api/v1/reply (AI response)
        → save message (PENDING)
        → enqueue to Redis

OutboundWorker (polls Redis every 200ms):
    → dequeue message
    → call channel-adapter POST /api/v1/messages/send
    → update status: SENT or FAILED (with retry)
```

## Testing

```bash
cd services/inbox-service
./gradlew test    # 16 unit tests
```

## Environment Variables

| Variable | Description |
|---|---|
| DATABASE_URL | PostgreSQL JDBC URL |
| REDIS_HOST | Redis host |
| REDIS_PORT | Redis port |
| CHANNEL_ADAPTER_URL | Channel adapter service URL |
| AI_ENGINE_URL | AI engine service URL |
| AUTH_SERVICE_URL | Auth service URL for API key validation |
| SERVICE_API_KEY | API key for calling channel-adapter (outbound) |
