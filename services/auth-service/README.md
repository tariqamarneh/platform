# Auth Service

Authentication, user management, and API key management microservice for the Business Agent platform.

## What it does

- **Business registration** — Creates a tenant (Business) and the first user (OWNER) in a single transaction
- **JWT authentication** — Stateless access tokens (15 min) with rotating refresh tokens (7 days)
- **API key management** — Create, verify, list, and revoke API keys for service-to-service auth
- **User profiles** — Retrieve the current authenticated user's profile

## Tech Stack

| | |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4, Spring Security |
| Database | PostgreSQL 17, Flyway migrations |
| Cache | Redis 7 (API key verification cache) |
| Auth | JWT (JJWT), BCrypt |
| API Docs | Swagger UI (SpringDoc OpenAPI) |
| Build | Gradle 9.4 |

## Quick Start

From the platform root:

```bash
docker compose up --build
```

The service starts on **http://localhost:8080** with Postgres and Redis.

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Health check: http://localhost:8080/health
- Actuator: http://localhost:8080/actuator/health

## API Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | /api/v1/auth/register | No | Register business + owner |
| POST | /api/v1/auth/login | No | Login, get tokens |
| POST | /api/v1/auth/refresh | No | Refresh access token |
| GET | /api/v1/users/me | JWT | Get current user |
| POST | /api/v1/keys | JWT | Create API key |
| POST | /api/v1/keys/verify | X-API-Key | Verify API key |
| GET | /api/v1/keys | JWT | List active keys |
| DELETE | /api/v1/keys/{id} | JWT | Revoke a key |

## Testing

```bash
# Unit tests (55 tests, no dependencies needed)
./gradlew test

# E2E tests (36 tests, requires running service)
./scripts/e2e-test.sh

# Concurrency tests (8 tests, requires running service)
./scripts/concurrency-test.sh
```

## Documentation

- [Architecture](docs/architecture.md) — Domain model, auth flows, concurrency model, caching
- [API Reference](docs/api.md) — All endpoints with request/response examples
- [Running](docs/running.md) — Docker Compose, local dev, environment variables
- [Testing](docs/testing.md) — Unit tests, E2E tests, concurrency tests, test scripts
