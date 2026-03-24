# Business Agent Platform

A SaaS platform that replaces human customer service teams with AI-powered WhatsApp chatbots. Businesses connect their WhatsApp Business Account (WABA) via the Meta WhatsApp API, and the platform handles customer inquiries automatically.

## How it works

1. A business signs up and connects their WhatsApp account
2. They configure their AI bot with business-specific knowledge
3. When a customer sends a WhatsApp message, Meta's webhook forwards it to the platform
4. The AI engine processes the message and sends an intelligent response back via WhatsApp

## Architecture

```
Customer (WhatsApp) → Meta API → Platform → AI Response → Meta API → Customer
```

The platform is a monorepo with independently deployable microservices:

| Service | Tech | Port | Status | Description |
|---|---|---|---|---|
| [auth-service](services/auth-service/) | Java 21, Spring Boot 3.4 | 8080 | Implemented | Authentication, user management, API key management |
| [channel-adapter](services/channel-adapter/) | Java 21, Spring Boot 3.4 | 8081 | Implemented | WhatsApp webhook ingestion, message sending, channel management |
| [ai-engine](services/ai-engine/) | Python 3.13, FastAPI | 8083 | Scaffolded | AI/ML processing |
| [web](services/web/) | Next.js 16, React 19 | 3000 | Landing + Auth | Landing page, login/register, admin panel |

### Service communication

```
Customer (WhatsApp) → Meta API → channel-adapter → inbox-service → ai-engine
                                       ↑                              ↓
                                  Meta API  ←─────── channel-adapter ←┘
                                                          ↕
web (dashboard) ──JWT──→ auth-service ←──API Key──── channel-adapter
```

- **Meta → channel-adapter**: WhatsApp webhooks (inbound messages)
- **channel-adapter → inbox-service**: Normalized messages forwarded via HTTP
- **inbox-service → ai-engine**: AI response generation
- **inbox-service → channel-adapter**: Send replies back via WhatsApp
- **channel-adapter → auth-service**: API key validation
- **web → auth-service**: JWT authentication for dashboard

## Quick Start

### Run everything locally

```bash
# Start all services
docker compose up --build

# Web app:          http://localhost:3000
# Channel adapter:  http://localhost:8081
# Auth service: http://localhost:8080
# Swagger UI:   http://localhost:8080/swagger-ui/index.html
```

### Run individual services

```bash
# auth-service (Java 21, Gradle)
cd services/auth-service
docker compose up -d postgres redis   # start dependencies
./gradlew bootRun

# ai-engine (Python 3.13, Poetry)
cd services/ai-engine
poetry install
poetry run python -m ai_engine.main

# web (Next.js 16)
cd services/web
npm install
npm run dev
```

## Testing

```bash
# Auth service — unit tests
cd services/auth-service && ./gradlew test

# Auth service — E2E tests (requires running service)
./services/auth-service/scripts/e2e-test.sh

# Auth service — concurrency tests (requires running service)
./services/auth-service/scripts/concurrency-test.sh
```

## Deployment

Each service has its own Dockerfile and is designed to deploy independently to **Google Cloud Run** on GCP. Images will be stored in Google Artifact Registry.

## CI/CD

- **CI**: GitHub Actions runs on every pull request, with path-based filtering so only changed services are built and tested
- **CD**: Image build and push to Artifact Registry on merge to main (planned)

## Project Structure

```
platform/
├── docker-compose.yml              Local dev (Postgres, Redis, auth-service)
├── .github/workflows/ci.yml        CI pipeline
├── .editorconfig                    Formatting rules (Java/Python/TS)
├── services/
│   ├── auth-service/                Java microservice
│   │   ├── app/src/                 Source code
│   │   ├── docs/                    Architecture, API, running, testing docs
│   │   ├── scripts/                 E2E and concurrency test scripts
│   │   ├── Dockerfile               Multi-stage build
│   │   └── README.md
│   ├── ai-engine/                   Python microservice
│   │   ├── ai_engine/               Source code
│   │   ├── Dockerfile
│   │   └── pyproject.toml
│   └── web/                         Next.js frontend
│       ├── src/app/                 Source code
│       ├── Dockerfile
│       └── package.json
└── README.md
```
