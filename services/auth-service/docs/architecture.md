# Auth Service Architecture

## Overview

The auth-service is a Spring Boot 3.4 microservice responsible for authentication, user management, and API key management for the Business Agent platform. It is designed to run as a standalone container on Google Cloud Run.

## Domain Model

### Entities

- **Business** — The tenant. Every paying client is a Business. All other entities are scoped to a Business.
- **User** — A person who logs into the dashboard. Belongs to one Business. Has a role (OWNER, ADMIN, MEMBER).
- **RefreshToken** — An opaque token stored as a SHA-256 hash. Used to obtain new access tokens without re-entering credentials. Supports rotation and revocation.
- **ApiKey** — A long-lived credential for service-to-service authentication. Stored as a SHA-256 hash. The plaintext is returned only once at creation.

### Relationships

```
Business 1──* User
Business 1──* ApiKey
User     1──* RefreshToken
```

## Authentication Flow

### JWT (Dashboard Users)

1. User registers or logs in → receives an access token (JWT, 15 min) and a refresh token (opaque, 7 days)
2. Access token is sent as `Authorization: Bearer <token>` on every request
3. When the access token expires, the client sends the refresh token to get a new pair
4. Old refresh token is revoked on every refresh (rotation)

The access token contains: `userId`, `businessId`, `role`, `email`. It is stateless — the service never looks it up in the database.

### API Keys (Service-to-Service)

1. A business owner creates an API key via the dashboard
2. The plaintext key (prefixed `ba_live_`) is shown once and never stored
3. The ai-engine (or webhook handler) sends the key as `X-API-Key` header to `POST /api/v1/keys/verify`
4. Auth-service hashes the key, looks up the hash, and returns `{ valid: true, businessId: "..." }`

## Concurrency Model

- **Optimistic locking** (`@Version`) on Business, User, RefreshToken, and ApiKey entities prevents concurrent update conflicts
- **Refresh token double-spend** is prevented by optimistic locking — only the first concurrent request succeeds
- **Duplicate email registration** is protected by both application-level check and database unique constraint, with `DataIntegrityViolationException` handling for the race window
- All write operations are wrapped in `@Transactional`
- Read operations use `@Transactional(readOnly = true)` for connection pool optimization

## Caching

API key verification results are cached in Redis with a 5-minute TTL. The cache is evicted immediately when a key is revoked. This reduces database load for the most frequently called endpoint (every WhatsApp message triggers a verification).

## Security

- Passwords are hashed with BCrypt (strength 12)
- JWT signed with HMAC-SHA256, key cached at startup
- API keys stored as SHA-256 hashes — plaintext is never persisted
- All endpoints require authentication except: register, login, refresh, key verify, health, actuator, and swagger
- CSRF disabled (stateless API)
- Sessions disabled (stateless)
- Request correlation IDs (`X-Request-ID`) for cross-service tracing

## Tech Stack

| Component | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4 |
| Security | Spring Security + JJWT |
| Database | PostgreSQL 17 |
| Migrations | Flyway |
| Cache | Redis 7 via Spring Cache |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Build | Gradle 9.4 (Kotlin DSL) |
| Container | Eclipse Temurin 21 (multi-stage Docker) |
