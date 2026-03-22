# Auth Service API Reference

Base URL: `http://localhost:8080`

Interactive docs: `http://localhost:8080/swagger-ui/index.html`

## Authentication

### Register
```
POST /api/v1/auth/register
```
Creates a new business and owner account.

**Request body:**
| Field | Type | Validation |
|---|---|---|
| businessName | string | Required, max 255 |
| email | string | Required, valid email |
| password | string | Required, 8-100 chars |
| firstName | string | Required, max 255 |
| lastName | string | Required, max 255 |

**Response:** `201 Created`
```json
{
  "accessToken": "eyJhbGci...",
  "refreshToken": "aac2b731...",
  "expiresIn": 900
}
```

### Login
```
POST /api/v1/auth/login
```
**Request body:** `email`, `password`

**Response:** `200 OK` — same shape as register

### Refresh Token
```
POST /api/v1/auth/refresh
```
**Request body:** `refreshToken`

**Response:** `200 OK` — new token pair. Old refresh token is revoked.

## Users

### Get Current User
```
GET /api/v1/users/me
Authorization: Bearer <access_token>
```
**Response:** `200 OK`
```json
{
  "id": "uuid",
  "email": "owner@test.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "OWNER",
  "businessId": "uuid"
}
```

## API Keys

### Create Key
```
POST /api/v1/keys
Authorization: Bearer <access_token>
Content-Type: application/json
```
**Request body:** `{ "name": "Production Key" }`

**Response:** `201 Created`
```json
{
  "id": "uuid",
  "name": "Production Key",
  "keyPrefix": "ba_live_cf6f",
  "rawKey": "ba_live_cf6f045e...",
  "createdAt": "2026-03-22T12:16:05"
}
```
The `rawKey` is returned only once. Store it securely.

### Verify Key
```
POST /api/v1/keys/verify
X-API-Key: ba_live_cf6f045e...
```
No authentication required. Used by ai-engine.

**Response:** `200 OK`
```json
{ "valid": true, "businessId": "uuid" }
```
or
```json
{ "valid": false, "businessId": null }
```

### List Keys
```
GET /api/v1/keys
Authorization: Bearer <access_token>
```
**Response:** `200 OK` — array of active keys (without raw key values)

### Revoke Key
```
DELETE /api/v1/keys/{id}
Authorization: Bearer <access_token>
```
**Response:** `204 No Content`

## Health & Monitoring

| Endpoint | Purpose |
|---|---|
| `GET /health` | Simple health probe for Cloud Run |
| `GET /actuator/health` | Rich health with DB and Redis status |
| `GET /actuator/health/liveness` | Liveness probe |
| `GET /actuator/health/readiness` | Readiness probe |

## Error Responses

All errors follow this format:
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Email already registered",
  "timestamp": "2026-03-22T12:15:31"
}
```

| Status | When |
|---|---|
| 400 | Validation failure |
| 401 | Bad credentials or invalid/expired token |
| 403 | Missing authentication |
| 404 | Resource not found |
| 409 | Duplicate resource or concurrent update conflict |
| 500 | Unexpected server error |

## Request Tracing

Every response includes an `X-Request-ID` header. If you send one in the request, it is propagated; otherwise one is generated. This ID appears in all log lines for that request.
