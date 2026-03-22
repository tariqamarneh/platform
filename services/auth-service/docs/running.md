# Running the Auth Service

## Prerequisites

- Docker and Docker Compose
- Java 21 (for running tests locally without Docker)

## Running with Docker Compose (recommended)

From the platform root:

```bash
cd ~/Desktop/platform
docker compose up --build
```

This starts:
- **PostgreSQL 17** on port 5432
- **Redis 7** on port 6379
- **auth-service** on port 8080

The service waits for Postgres and Redis health checks before starting. Flyway runs migrations automatically on startup.

### Verify it's running

```bash
curl http://localhost:8080/health
# {"status":"ok"}

curl http://localhost:8080/actuator/health
# {"status":"UP","groups":["liveness","readiness"]}
```

### View logs

```bash
docker compose logs -f auth-service
```

### Stop

```bash
docker compose down        # stop containers, keep data
docker compose down -v     # stop and delete volumes (fresh start)
```

## Running locally (without Docker)

You need PostgreSQL and Redis running locally.

```bash
# Start just the dependencies
docker compose up -d postgres redis

# Run the service with Gradle
cd services/auth-service
./gradlew bootRun
```

### Environment variables

| Variable | Default | Description |
|---|---|---|
| PORT | 8080 | Server port |
| DATABASE_URL | jdbc:postgresql://localhost:5432/auth | PostgreSQL JDBC URL |
| DATABASE_USER | postgres | Database username |
| DATABASE_PASSWORD | postgres | Database password |
| REDIS_HOST | localhost | Redis host |
| REDIS_PORT | 6379 | Redis port |
| REDIS_PASSWORD | (empty) | Redis password |
| JWT_SECRET | (dev default) | HMAC-SHA256 signing key (min 32 bytes) |

## Verify with test scripts

Once running, you can validate everything works:

```bash
# Run all E2E tests (36 tests)
./scripts/e2e-test.sh

# Run concurrency tests (8 tests)
./scripts/concurrency-test.sh
```

## Interactive API docs

Once running, open: http://localhost:8080/swagger-ui/index.html

Use the "Authorize" button to set your JWT token for authenticated endpoints.
