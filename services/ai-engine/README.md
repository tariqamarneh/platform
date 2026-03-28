# AI Engine

AI processing service for the Business Agent platform. Currently in **mock mode** for end-to-end testing.

## Current State: Mock

Generates canned responses based on message patterns (greetings, help, pricing, etc.). No real AI model — this is a placeholder for the full Claude/OpenAI integration.

## How it works

1. Inbox-service sends a notification when a customer message arrives
2. AI engine processes it in the background:
   - Sets typing indicator via inbox-service
   - Gets conversation history via inbox-service
   - Generates a response (mock)
   - Sends the reply via inbox-service
3. Inbox-service enqueues the reply for delivery via channel-adapter

## Tech Stack

| | |
|---|---|
| Language | Python 3.13 |
| Framework | FastAPI |
| HTTP Client | httpx |
| Build | Poetry |

## Quick Start

```bash
# With Docker Compose (from platform root)
docker compose up --build

# Or locally
cd services/ai-engine
poetry install
PORT=8083 INBOX_SERVICE_URL=http://localhost:8082 poetry run python -m ai_engine.main
```

## API Endpoints

| Method | Path | Description |
|---|---|---|
| POST | /api/v1/process | Process a new inbound message (async) |
| GET | /health | Health check |

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| PORT | 8083 | Server port |
| INBOX_SERVICE_URL | http://localhost:8082 | Inbox service URL |
