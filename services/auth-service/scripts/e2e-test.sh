#!/usr/bin/env bash
# ==============================================================================
# Auth Service — End-to-End Test Suite
#
# Usage:
#   ./scripts/e2e-test.sh              # run against localhost:8080
#   ./scripts/e2e-test.sh http://host  # run against custom base URL
#
# Prerequisites: curl, python3 (for JSON parsing)
# ==============================================================================

set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"
PASSED=0
FAILED=0
TOTAL=0
UNIQUE_ID=$(date +%s)

# --- Helpers ------------------------------------------------------------------

red()   { echo -e "\033[0;31m$1\033[0m"; }
green() { echo -e "\033[0;32m$1\033[0m"; }
bold()  { echo -e "\033[1m$1\033[0m"; }

json_field() {
  python3 -c "import sys,json; print(json.load(sys.stdin).get('$1',''))"
}

assert_status() {
  local test_name="$1"
  local expected="$2"
  local actual="$3"
  TOTAL=$((TOTAL + 1))
  if [ "$actual" = "$expected" ]; then
    green "  PASS: $test_name (HTTP $actual)"
    PASSED=$((PASSED + 1))
  else
    red "  FAIL: $test_name — expected HTTP $expected, got HTTP $actual"
    FAILED=$((FAILED + 1))
  fi
}

assert_json() {
  local test_name="$1"
  local field="$2"
  local expected="$3"
  local body="$4"
  TOTAL=$((TOTAL + 1))
  local actual
  actual=$(echo "$body" | python3 -c "import sys,json; print(json.load(sys.stdin).get('$field',''))" 2>/dev/null || echo "PARSE_ERROR")
  if [ "$actual" = "$expected" ]; then
    green "  PASS: $test_name ($field=$actual)"
    PASSED=$((PASSED + 1))
  else
    red "  FAIL: $test_name — expected $field=$expected, got $field=$actual"
    FAILED=$((FAILED + 1))
  fi
}

api() {
  local method="$1"
  local path="$2"
  shift 2
  curl -s -w "\n%{http_code}" -X "$method" "${BASE_URL}${path}" "$@"
}

parse_response() {
  BODY=$(echo "$RESPONSE" | sed '$d')
  CODE=$(echo "$RESPONSE" | tail -1)
}

# --- Wait for service ---------------------------------------------------------

bold "\nWaiting for service at $BASE_URL ..."
for i in $(seq 1 30); do
  if curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/health" | grep -q "200"; then
    green "Service is ready.\n"
    break
  fi
  if [ "$i" = "30" ]; then
    red "Service not ready after 60s. Aborting."
    exit 1
  fi
  sleep 2
done

# ==============================================================================
bold "=== 1. HEALTH & INFRASTRUCTURE ==="
# ==============================================================================

RESPONSE=$(api GET /health); parse_response
assert_status "GET /health" "200" "$CODE"
assert_json "health status" "status" "ok" "$BODY"

RESPONSE=$(api GET /actuator/health); parse_response
assert_status "GET /actuator/health" "200" "$CODE"

RESPONSE=$(api GET /api-docs); parse_response
assert_status "GET /api-docs (OpenAPI)" "200" "$CODE"

RESPONSE=$(api GET /swagger-ui/index.html); parse_response
assert_status "GET /swagger-ui" "200" "$CODE"

# Check X-Request-ID header
REQUEST_ID=$(curl -s -D - -o /dev/null "$BASE_URL/health" | grep -i "x-request-id" | tr -d '\r' | awk '{print $2}')
TOTAL=$((TOTAL + 1))
if [ -n "$REQUEST_ID" ]; then
  green "  PASS: X-Request-ID header present ($REQUEST_ID)"
  PASSED=$((PASSED + 1))
else
  red "  FAIL: X-Request-ID header missing"
  FAILED=$((FAILED + 1))
fi

# ==============================================================================
bold "\n=== 2. REGISTRATION ==="
# ==============================================================================

EMAIL="e2e-${UNIQUE_ID}@test.com"

RESPONSE=$(api POST /api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"businessName\":\"E2E Corp\",\"email\":\"$EMAIL\",\"password\":\"SecurePass123\",\"firstName\":\"Test\",\"lastName\":\"User\"}")
parse_response
assert_status "Register valid user" "201" "$CODE"

ACCESS_TOKEN=$(echo "$BODY" | json_field accessToken)
REFRESH_TOKEN=$(echo "$BODY" | json_field refreshToken)

TOTAL=$((TOTAL + 1))
if [ -n "$ACCESS_TOKEN" ] && [ "$ACCESS_TOKEN" != "" ]; then
  green "  PASS: Access token returned"
  PASSED=$((PASSED + 1))
else
  red "  FAIL: No access token in response"
  FAILED=$((FAILED + 1))
fi

# Duplicate email
RESPONSE=$(api POST /api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"businessName\":\"Dupe\",\"email\":\"$EMAIL\",\"password\":\"SecurePass123\",\"firstName\":\"A\",\"lastName\":\"B\"}")
parse_response
assert_status "Register duplicate email" "409" "$CODE"

# Missing fields
RESPONSE=$(api POST /api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"incomplete@test.com"}')
parse_response
assert_status "Register missing fields" "400" "$CODE"

# Invalid email
RESPONSE=$(api POST /api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"businessName":"X","email":"notanemail","password":"SecurePass123","firstName":"A","lastName":"B"}')
parse_response
assert_status "Register invalid email" "400" "$CODE"

# Short password
RESPONSE=$(api POST /api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"businessName":"X","email":"short@test.com","password":"123","firstName":"A","lastName":"B"}')
parse_response
assert_status "Register short password" "400" "$CODE"

# ==============================================================================
bold "\n=== 3. LOGIN ==="
# ==============================================================================

RESPONSE=$(api POST /api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"SecurePass123\"}")
parse_response
assert_status "Login valid credentials" "200" "$CODE"
ACCESS_TOKEN=$(echo "$BODY" | json_field accessToken)
REFRESH_TOKEN=$(echo "$BODY" | json_field refreshToken)

RESPONSE=$(api POST /api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"WrongPass\"}")
parse_response
assert_status "Login wrong password" "401" "$CODE"

RESPONSE=$(api POST /api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"nobody@test.com","password":"SecurePass123"}')
parse_response
assert_status "Login non-existent email" "401" "$CODE"

# ==============================================================================
bold "\n=== 4. USER PROFILE ==="
# ==============================================================================

RESPONSE=$(api GET /api/v1/users/me \
  -H "Authorization: Bearer $ACCESS_TOKEN")
parse_response
assert_status "Get current user (authenticated)" "200" "$CODE"
assert_json "User email" "email" "$EMAIL" "$BODY"
assert_json "User role" "role" "OWNER" "$BODY"

RESPONSE=$(api GET /api/v1/users/me)
parse_response
assert_status "Get current user (no token)" "403" "$CODE"

RESPONSE=$(api GET /api/v1/users/me \
  -H "Authorization: Bearer invalid.token.here")
parse_response
assert_status "Get current user (invalid token)" "403" "$CODE"

# ==============================================================================
bold "\n=== 5. TOKEN REFRESH ==="
# ==============================================================================

RESPONSE=$(api POST /api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}")
parse_response
assert_status "Refresh token (valid)" "200" "$CODE"
NEW_ACCESS=$(echo "$BODY" | json_field accessToken)

# Reuse old refresh token (should be revoked)
RESPONSE=$(api POST /api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}")
parse_response
assert_status "Reuse revoked refresh token" "401" "$CODE"

# Invalid refresh token
RESPONSE=$(api POST /api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"totally-fake-token"}')
parse_response
assert_status "Refresh with invalid token" "401" "$CODE"

# Update access token
ACCESS_TOKEN="$NEW_ACCESS"

# ==============================================================================
bold "\n=== 6. API KEYS ==="
# ==============================================================================

# Create
RESPONSE=$(api POST /api/v1/keys \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{"name":"E2E Test Key"}')
parse_response
assert_status "Create API key" "201" "$CODE"
RAW_KEY=$(echo "$BODY" | json_field rawKey)
KEY_ID=$(echo "$BODY" | json_field id)

TOTAL=$((TOTAL + 1))
if echo "$RAW_KEY" | grep -q "^ba_live_"; then
  green "  PASS: Raw key has ba_live_ prefix"
  PASSED=$((PASSED + 1))
else
  red "  FAIL: Raw key missing ba_live_ prefix: $RAW_KEY"
  FAILED=$((FAILED + 1))
fi

# Create without auth
RESPONSE=$(api POST /api/v1/keys \
  -H "Content-Type: application/json" \
  -d '{"name":"No Auth Key"}')
parse_response
assert_status "Create API key (no auth)" "403" "$CODE"

# List
RESPONSE=$(api GET /api/v1/keys \
  -H "Authorization: Bearer $ACCESS_TOKEN")
parse_response
assert_status "List API keys" "200" "$CODE"

# Verify (valid)
RESPONSE=$(api POST /api/v1/keys/verify \
  -H "X-API-Key: $RAW_KEY")
parse_response
assert_status "Verify key (valid)" "200" "$CODE"
assert_json "Key valid" "valid" "True" "$BODY"

# Verify (cached — second call)
RESPONSE=$(api POST /api/v1/keys/verify \
  -H "X-API-Key: $RAW_KEY")
parse_response
assert_status "Verify key (cached)" "200" "$CODE"
assert_json "Key still valid (cached)" "valid" "True" "$BODY"

# Verify (invalid)
RESPONSE=$(api POST /api/v1/keys/verify \
  -H "X-API-Key: ba_live_doesnotexistfakekey123456")
parse_response
assert_status "Verify key (invalid)" "200" "$CODE"
assert_json "Key invalid" "valid" "False" "$BODY"

# Revoke
RESPONSE=$(api DELETE "/api/v1/keys/$KEY_ID" \
  -H "Authorization: Bearer $ACCESS_TOKEN")
parse_response
assert_status "Revoke API key" "204" "$CODE"

# Verify after revoke (cache should be evicted)
RESPONSE=$(api POST /api/v1/keys/verify \
  -H "X-API-Key: $RAW_KEY")
parse_response
assert_status "Verify key (after revoke)" "200" "$CODE"
assert_json "Key invalid after revoke" "valid" "False" "$BODY"

# ==============================================================================
bold "\n=== 7. SUPER ADMIN ==="
# ==============================================================================

# Admin login
RESPONSE=$(api POST /api/v1/admin/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@businessagent.com","password":"SuperAdmin123!"}')
parse_response
assert_status "Admin login" "200" "$CODE"
ADMIN_TOKEN=$(echo "$BODY" | json_field accessToken)

TOTAL=$((TOTAL + 1))
if [ -n "$ADMIN_TOKEN" ] && [ "$ADMIN_TOKEN" != "" ]; then
  green "  PASS: Admin access token returned"
  PASSED=$((PASSED + 1))
else
  red "  FAIL: No admin access token in response"
  FAILED=$((FAILED + 1))
fi

# Admin login wrong password
RESPONSE=$(api POST /api/v1/admin/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@businessagent.com","password":"WrongPass"}')
parse_response
assert_status "Admin login wrong password" "401" "$CODE"

# Admin login non-existent email
RESPONSE=$(api POST /api/v1/admin/login \
  -H "Content-Type: application/json" \
  -d '{"email":"nobody@admin.com","password":"Whatever123"}')
parse_response
assert_status "Admin login non-existent email" "401" "$CODE"

# Get stats
RESPONSE=$(api GET /api/v1/admin/stats \
  -H "Authorization: Bearer $ADMIN_TOKEN")
parse_response
assert_status "Admin get stats" "200" "$CODE"

# List businesses
RESPONSE=$(api GET "/api/v1/admin/businesses?page=0&size=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
parse_response
assert_status "Admin list businesses" "200" "$CODE"

# Get single business (extract first business id from the page)
FIRST_BIZ_ID=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['content'][0]['id'] if d.get('content') else '')" 2>/dev/null || echo "")
if [ -n "$FIRST_BIZ_ID" ]; then
  RESPONSE=$(api GET "/api/v1/admin/businesses/$FIRST_BIZ_ID" \
    -H "Authorization: Bearer $ADMIN_TOKEN")
  parse_response
  assert_status "Admin get business detail" "200" "$CODE"

  # Get business users
  RESPONSE=$(api GET "/api/v1/admin/businesses/$FIRST_BIZ_ID/users?page=0&size=10" \
    -H "Authorization: Bearer $ADMIN_TOKEN")
  parse_response
  assert_status "Admin list business users" "200" "$CODE"

  # Update business plan
  RESPONSE=$(api PATCH "/api/v1/admin/businesses/$FIRST_BIZ_ID" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d '{"plan":"PAID"}')
  parse_response
  assert_status "Admin update business plan" "200" "$CODE"
  assert_json "Business plan updated" "plan" "PAID" "$BODY"

  # Suspend business
  RESPONSE=$(api PATCH "/api/v1/admin/businesses/$FIRST_BIZ_ID" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d '{"status":"SUSPENDED"}')
  parse_response
  assert_status "Admin suspend business" "200" "$CODE"
  assert_json "Business status suspended" "status" "SUSPENDED" "$BODY"

  # Reactivate business
  RESPONSE=$(api PATCH "/api/v1/admin/businesses/$FIRST_BIZ_ID" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d '{"status":"ACTIVE"}')
  parse_response
  assert_status "Admin reactivate business" "200" "$CODE"
  assert_json "Business status active" "status" "ACTIVE" "$BODY"
fi

# Admin endpoints without token (should be 403)
RESPONSE=$(api GET /api/v1/admin/stats)
parse_response
assert_status "Admin stats without token" "403" "$CODE"

# Admin endpoints with regular user token (should be 403)
RESPONSE=$(api GET /api/v1/admin/stats \
  -H "Authorization: Bearer $ACCESS_TOKEN")
parse_response
assert_status "Admin stats with regular user token" "403" "$CODE"

# ==============================================================================
bold "\n=== RESULTS ==="
# ==============================================================================

echo ""
if [ "$FAILED" -eq 0 ]; then
  green "ALL $TOTAL TESTS PASSED"
else
  red "$FAILED/$TOTAL TESTS FAILED"
  green "$PASSED/$TOTAL TESTS PASSED"
fi
echo ""

exit "$FAILED"
