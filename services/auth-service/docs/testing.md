# Testing the Auth Service

## Test Scripts

The service includes automated test scripts in `scripts/`. Start the service with Docker Compose first, then run:

```bash
# E2E tests — 36 tests covering every endpoint and error path
./scripts/e2e-test.sh

# Concurrency tests — 8 tests validating race condition handling
./scripts/concurrency-test.sh

# Against a custom URL
./scripts/e2e-test.sh https://auth.staging.example.com
```

Both scripts exit with code 0 on success or the number of failures on failure.

## Unit Tests

Run all 55 unit tests:

```bash
cd services/auth-service
./gradlew test
```

Tests use H2 in-memory database in PostgreSQL compatibility mode. No external dependencies required.

### Test report

After running, open: `app/build/reports/tests/test/index.html`

### Test structure

| Test Class | Scope | Tests |
|---|---|---|
| ApiKeyGeneratorTest | Utility | Key generation, hashing, prefix |
| JwtProviderTest | Security | Token generation, validation, claims |
| AuthenticatedUserTest | Security | UserDetails contract |
| AuthServiceImplTest | Service | Register, login, refresh flows |
| UserServiceImplTest | Service | Get current user |
| ApiKeyServiceImplTest | Service | Create, verify, revoke, list keys |
| UserConverterTest | Converter | Entity to DTO mapping |
| ApiKeyConverterTest | Converter | Entity to DTO mapping |
| AuthControllerTest | Controller | Registration/login validation |
| ApiKeyControllerTest | Controller | Key endpoints |
| UserControllerTest | Controller | User profile endpoint |
| HealthControllerTest | Controller | Health check |

## E2E Tests (scripts/e2e-test.sh)

Automated script that tests every endpoint against a running instance. Covers:

- Health checks, actuator, Swagger UI, and request ID headers
- Registration: valid, duplicate email, missing fields, invalid email, short password
- Login: valid credentials, wrong password, non-existent email
- User profile: authenticated, unauthenticated, invalid token
- Token refresh: valid, reuse revoked token, invalid token
- API keys: create, create without auth, list, verify, verify from cache, verify invalid, revoke, verify after revoke

## Concurrency Tests (scripts/concurrency-test.sh)

Automated script that validates race condition handling:

| Test | Parallel Requests | Expected |
|---|---|---|
| Duplicate registration | 10 with same email | 1x 201, 9x 409 |
| Refresh token double-spend | 5 with same token | 1x 200, 4x 401/409 |
| Concurrent key creation | 10 | 10x 201 |
| Concurrent key verification | 20 | 20x 200 |
| Concurrent logins | 20 | 20x 200 |

### How the concurrency protections work

- **Duplicate registration**: Application-level `existsByEmail` check + database unique constraint on `email` + `DataIntegrityViolationException` handler for the TOCTOU race window
- **Refresh token double-spend**: `@Version` (optimistic locking) on RefreshToken entity — first transaction to commit wins, others get `OptimisticLockingFailureException`
- **Concurrent key creation**: Each key gets a unique random value, no conflicts possible
- **Concurrent verification**: Redis cache absorbs load after the first DB hit
- **Concurrent logins**: Each login creates an independent refresh token, no shared mutable state
