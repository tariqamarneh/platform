# AI Engine Agent Rules

## Service Overview
AI processing service for the Business Agent platform. Currently in **mock mode** — generates canned responses. Will be replaced with real AI (Claude/OpenAI) when ready.

## Current Mode: Mock
- Responds to greetings, thank-you, help, pricing, and hours questions with canned responses
- Default: random helpful response from a predefined list
- No AI model, no knowledge base, no embeddings — just pattern matching

## Flow
```
inbox-service POST /api/v1/process → ai-engine
    → background task:
        1. Set typing indicator (POST inbox-service /api/v1/typing)
        2. Get conversation history (GET inbox-service /api/v1/conversations/{id}/messages)
        3. Generate response (mock_ai.generate_response)
        4. Send reply (POST inbox-service /api/v1/reply)
```

## Project Structure
```
ai_engine/
├── main.py              FastAPI app, health endpoint
├── config.py            Environment variables
├── inbox_client.py      HTTP client for inbox-service
├── mock_ai.py           Mock response generator (replace with real AI)
└── router/
    └── process.py       POST /api/v1/process endpoint
```

## Replacing Mock with Real AI
1. Replace `mock_ai.py` with a module that calls Claude/OpenAI API
2. Add per-business knowledge base lookup (from a config service or DB)
3. Add conversation context building from history
4. The router and inbox_client stay the same — only the response generation changes

## Environment Variables
- `PORT` — Server port (default: 8083)
- `INBOX_SERVICE_URL` — Inbox service base URL
