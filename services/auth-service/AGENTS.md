# Auth Service Agent Rules

## Service Overview
This is a Java 21 Spring Boot 3.4 microservice for authentication, user management, and API key management. It uses PostgreSQL for persistence, Redis for caching, and JWT for stateless authentication.

## Project Structure
```
app/src/main/java/com/businessagent/auth/
├── config/        Spring configuration (Security, JWT, Cache, OpenAPI, RequestIdFilter)
├── controller/    REST controllers (Auth, User, ApiKey, Health)
├── converter/     Entity → DTO mappers
├── dto/           Request and response records
├── exception/     Custom exceptions + GlobalExceptionHandler
├── model/         JPA entities (BaseEntity, BaseEntityNoUpdate, Business, User, RefreshToken, ApiKey)
├── repository/    Spring Data JPA repositories
├── security/      JWT provider, auth filter, AuthenticatedUser
├── service/       Business logic interfaces and implementations
└── util/          ApiKeyGenerator utility
```

## Key Conventions
- **Entities** extend `BaseEntity` (with `updatedAt`) or `BaseEntityNoUpdate` (without). Both provide UUID id, createdAt, equals/hashCode.
- **DTOs** are Java records with Jakarta Bean Validation annotations.
- **Services** use constructor injection via Lombok `@RequiredArgsConstructor`. All write methods are `@Transactional`, reads are `@Transactional(readOnly = true)`.
- **Optimistic locking** (`@Version`) is on all entities. Always handle `OptimisticLockingFailureException`.
- **Caching** uses Spring `@Cacheable`/`CacheManager` with Redis. Cache name: `apiKeyVerification`.
- **Logging** uses SLF4J with MDC `requestId`. Never log secrets (passwords, tokens, API keys).

## Build and Test
```bash
./gradlew build          # compile + test + bootJar
./gradlew test           # tests only
./gradlew bootRun        # run locally (needs Postgres + Redis)
```

Tests use H2 in PostgreSQL compatibility mode. Test config is at `app/src/test/resources/application-test.yml`.

## Database
- Migrations in `app/src/main/resources/db/migration/` (Flyway, V1-V4)
- `ddl-auto: validate` in production — schema changes must go through Flyway
- Tables: `businesses`, `users`, `refresh_tokens`, `api_keys`

## Security Rules
- Public endpoints: `/api/v1/auth/**`, `POST /api/v1/keys/verify`, `/health`, `/actuator/**`, `/swagger-ui/**`, `/api-docs/**`
- All other endpoints require a valid JWT in `Authorization: Bearer <token>`
- API key verification uses `X-API-Key` header (not query parameter — never put secrets in URLs)
- Passwords: BCrypt strength 12
- JWT: HMAC-SHA256, secret must be >= 32 bytes

## Adding New Endpoints
1. Create the DTO record(s) in `dto/request/` and `dto/response/`
2. Add the service interface method, then implement it
3. Create the controller method with `@Valid`, proper HTTP status, and `@Operation` annotation
4. If the endpoint is public, add it to `SecurityConfig` requestMatchers AND `JwtAuthenticationFilter.shouldNotFilter`
5. Write unit tests for the service (Mockito) and controller (`@WebMvcTest`)

## Common Pitfalls
- Spring AOP proxies do not intercept internal method calls. If you need `@CacheEvict` or `@Transactional` on a method, call it through the proxy (inject self or use `CacheManager` directly).
- `@MockitoBean` (not `@MockBean`) is the correct annotation for Spring Boot 3.4+ tests.
- The `JwtAuthenticationFilter.shouldNotFilter` must be kept in sync with `SecurityConfig` permitAll paths.
