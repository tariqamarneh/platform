# Platform

Monorepo for the Business Agent platform. Contains three independently deployable microservices.

## Services

| Service | Tech | Path | Description |
|---|---|---|---|
| **auth-service** | Java 21, Gradle | `services/auth-service/` | Authentication and authorization microservice |
| **ai-engine** | Python 3.13, Poetry | `services/ai-engine/` | AI/ML processing microservice |
| **web** | Next.js 16, React 19 | `services/web/` | Frontend web application |

## Getting Started

Each service runs independently with its own build tooling:

### auth-service
```bash
cd services/auth-service
./gradlew run
```

### ai-engine
```bash
cd services/ai-engine
poetry install
poetry run python -m ai_engine.main
```

### web
```bash
cd services/web
npm install
npm run dev
```

## Deployment

Each service is designed to be containerized individually and deployed to Google Cloud Run.

## Project Structure

```
platform/
├── services/
│   ├── auth-service/   Java microservice (Gradle)
│   ├── ai-engine/      Python microservice (Poetry)
│   └── web/            Next.js frontend
└── README.md
```
