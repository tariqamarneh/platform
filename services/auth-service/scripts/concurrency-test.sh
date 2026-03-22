#!/usr/bin/env bash
# ==============================================================================
# Auth Service — Concurrency Test Suite
#
# Usage:
#   ./scripts/concurrency-test.sh              # run against localhost:8080
#   ./scripts/concurrency-test.sh http://host  # run against custom base URL
#
# Tests race conditions: duplicate registration, refresh token double-spend,
# concurrent key operations, and concurrent logins.
#
# Prerequisites: curl, python3
# ==============================================================================

set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"
PASSED=0
FAILED=0
TOTAL=0
UNIQUE_ID=$(date +%s)

red()   { echo -e "\033[0;31m$1\033[0m"; }
green() { echo -e "\033[0;32m$1\033[0m"; }
bold()  { echo -e "\033[1m$1\033[0m"; }

json_field() {
  python3 -c "import sys,json; print(json.load(sys.stdin).get('$1',''))"
}

assert_count() {
  local test_name="$1"
  local expected_code="$2"
  local expected_count="$3"
  local actual_count="$4"
  local total_requests="$5"
  TOTAL=$((TOTAL + 1))
  if [ "$actual_count" = "$expected_count" ]; then
    green "  PASS: $test_name — $actual_count/$total_requests got HTTP $expected_code"
    PASSED=$((PASSED + 1))
  else
    red "  FAIL: $test_name — expected $expected_count x HTTP $expected_code, got $actual_count"
    FAILED=$((FAILED + 1))
  fi
}

# --- Wait for service ---------------------------------------------------------

bold "\nWaiting for service at $BASE_URL ..."
for i in $(seq 1 30); do
  if curl -s -o /dev/null -w "%{http_code}\n" "$BASE_URL/health" | grep -q "200"; then
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
bold "=== 1. CONCURRENT DUPLICATE REGISTRATION ==="
# ==============================================================================
bold "  Sending 10 parallel registrations with the same email..."

RACE_EMAIL="race-${UNIQUE_ID}@test.com"
TMPDIR_RACE=$(mktemp -d)

for i in $(seq 1 10); do
  curl -s -o /dev/null -w "%{http_code}\n" -X POST "$BASE_URL/api/v1/auth/register" \
    -H "Content-Type: application/json" \
    -d "{\"businessName\":\"Race Corp $i\",\"email\":\"$RACE_EMAIL\",\"password\":\"SecurePass123\",\"firstName\":\"User\",\"lastName\":\"$i\"}" \
    > "$TMPDIR_RACE/$i.txt" 2>/dev/null &
done
wait

CREATED=$(cat "$TMPDIR_RACE"/*.txt | grep -c "201" || true)
CONFLICT=$(cat "$TMPDIR_RACE"/*.txt | grep -c "409" || true)
rm -rf "$TMPDIR_RACE"

assert_count "Exactly 1 user created" "201" "1" "$CREATED" "10"
assert_count "9 rejected as duplicate" "409" "9" "$CONFLICT" "10"

# Verify only one user exists (login works)
LOGIN_CODE=$(curl -s -o /dev/null -w "%{http_code}\n" -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$RACE_EMAIL\",\"password\":\"SecurePass123\"}")
TOTAL=$((TOTAL + 1))
if [ "$LOGIN_CODE" = "200" ]; then
  green "  PASS: Login works for the one created user"
  PASSED=$((PASSED + 1))
else
  red "  FAIL: Login failed (HTTP $LOGIN_CODE)"
  FAILED=$((FAILED + 1))
fi

# ==============================================================================
bold "\n=== 2. CONCURRENT REFRESH TOKEN DOUBLE-SPEND ==="
# ==============================================================================
bold "  Getting a refresh token, then using it 5 times in parallel..."

LOGIN_RESP=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$RACE_EMAIL\",\"password\":\"SecurePass123\"}")
RT=$(echo "$LOGIN_RESP" | json_field refreshToken)

TMPDIR_REFRESH=$(mktemp -d)

for i in $(seq 1 5); do
  curl -s -o /dev/null -w "%{http_code}\n" -X POST "$BASE_URL/api/v1/auth/refresh" \
    -H "Content-Type: application/json" \
    -d "{\"refreshToken\":\"$RT\"}" \
    > "$TMPDIR_REFRESH/$i.txt" 2>/dev/null &
done
wait

SUCCESS=$(cat "$TMPDIR_REFRESH"/*.txt | grep -c "200" || true)
REJECTED=$(cat "$TMPDIR_REFRESH"/*.txt | grep -cE "401|409" || true)
rm -rf "$TMPDIR_REFRESH"

assert_count "Exactly 1 refresh succeeded" "200" "1" "$SUCCESS" "5"
assert_count "4 rejected (revoked/conflict)" "401|409" "4" "$REJECTED" "5"

# ==============================================================================
bold "\n=== 3. CONCURRENT API KEY CREATION ==="
# ==============================================================================
bold "  Creating 10 API keys in parallel..."

LOGIN_RESP=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$RACE_EMAIL\",\"password\":\"SecurePass123\"}")
AT=$(echo "$LOGIN_RESP" | json_field accessToken)

TMPDIR_KEYS=$(mktemp -d)

for i in $(seq 1 10); do
  curl -s -o /dev/null -w "%{http_code}\n" -X POST "$BASE_URL/api/v1/keys" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $AT" \
    -d "{\"name\":\"Concurrent Key $i\"}" \
    > "$TMPDIR_KEYS/$i.txt" 2>/dev/null &
done
wait

CREATED=$(cat "$TMPDIR_KEYS"/*.txt | grep -c "201" || true)
rm -rf "$TMPDIR_KEYS"

assert_count "All 10 keys created" "201" "10" "$CREATED" "10"

# ==============================================================================
bold "\n=== 4. CONCURRENT API KEY VERIFICATION ==="
# ==============================================================================
bold "  Creating one key, verifying it 20 times in parallel..."

KEY_RESP=$(curl -s -X POST "$BASE_URL/api/v1/keys" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $AT" \
  -d '{"name":"Verify Load Key"}')
RAW_KEY=$(echo "$KEY_RESP" | json_field rawKey)

TMPDIR_VERIFY=$(mktemp -d)

for i in $(seq 1 20); do
  curl -s -o /dev/null -w "%{http_code}\n" -X POST "$BASE_URL/api/v1/keys/verify" \
    -H "X-API-Key: $RAW_KEY" \
    > "$TMPDIR_VERIFY/$i.txt" 2>/dev/null &
done
wait

OK=$(cat "$TMPDIR_VERIFY"/*.txt | grep -c "200" || true)
rm -rf "$TMPDIR_VERIFY"

assert_count "All 20 verifications succeeded" "200" "20" "$OK" "20"

# ==============================================================================
bold "\n=== 5. CONCURRENT LOGINS ==="
# ==============================================================================
bold "  Logging in 20 times in parallel with the same user..."

TMPDIR_LOGIN=$(mktemp -d)

for i in $(seq 1 20); do
  curl -s -o /dev/null -w "%{http_code}\n" -X POST "$BASE_URL/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$RACE_EMAIL\",\"password\":\"SecurePass123\"}" \
    > "$TMPDIR_LOGIN/$i.txt" 2>/dev/null &
done
wait

OK=$(cat "$TMPDIR_LOGIN"/*.txt | grep -c "200" || true)
rm -rf "$TMPDIR_LOGIN"

assert_count "All 20 logins succeeded" "200" "20" "$OK" "20"

# ==============================================================================
bold "\n=== RESULTS ==="
# ==============================================================================

echo ""
if [ "$FAILED" -eq 0 ]; then
  green "ALL $TOTAL CONCURRENCY TESTS PASSED"
else
  red "$FAILED/$TOTAL TESTS FAILED"
  green "$PASSED/$TOTAL TESTS PASSED"
fi
echo ""

exit "$FAILED"
