#!/usr/bin/env bash
# ==============================================================================
# Channel Adapter — End-to-End Test Suite
#
# Usage:
#   ./scripts/e2e-test.sh                          # defaults
#   ./scripts/e2e-test.sh http://host:8081 http://host:8080
#
# Prerequisites: curl, python3, auth-service + channel-adapter running
# ==============================================================================

set -euo pipefail

ADAPTER_URL="${1:-http://localhost:8081}"
AUTH_URL="${2:-http://localhost:8080}"
PASSED=0
FAILED=0
TOTAL=0
UID_SUFFIX=$(date +%s)

red()   { echo -e "\033[0;31m$1\033[0m"; }
green() { echo -e "\033[0;32m$1\033[0m"; }
bold()  { echo -e "\033[1m$1\033[0m"; }

assert_status() {
  local name="$1" expected="$2" actual="$3"
  TOTAL=$((TOTAL + 1))
  if [ "$actual" = "$expected" ]; then
    green "  PASS: $name (HTTP $actual)"
    PASSED=$((PASSED + 1))
  else
    red "  FAIL: $name — expected HTTP $expected, got HTTP $actual"
    FAILED=$((FAILED + 1))
  fi
}

assert_json() {
  local name="$1" field="$2" expected="$3" body="$4"
  TOTAL=$((TOTAL + 1))
  local actual
  actual=$(echo "$body" | python3 -c "import sys,json; print(json.load(sys.stdin).get('$field',''))" 2>/dev/null || echo "PARSE_ERROR")
  if [ "$actual" = "$expected" ]; then
    green "  PASS: $name ($field=$actual)"
    PASSED=$((PASSED + 1))
  else
    red "  FAIL: $name — expected $field=$expected, got $field=$actual"
    FAILED=$((FAILED + 1))
  fi
}

api() {
  local method="$1" url="$2"; shift 2
  curl -s -w "\n%{http_code}" -X "$method" "$url" "$@"
}

parse() { BODY=$(echo "$RESPONSE" | sed '$d'); CODE=$(echo "$RESPONSE" | tail -1); }

# --- Wait for services --------------------------------------------------------

bold "\nWaiting for services..."
for i in $(seq 1 30); do
  curl -s "$AUTH_URL/health" > /dev/null 2>&1 && break; sleep 2
done
for i in $(seq 1 30); do
  curl -s "$ADAPTER_URL/health" > /dev/null 2>&1 && break; sleep 2
done
green "Services ready.\n"

# --- Setup: get an API key from auth-service ----------------------------------

bold "=== SETUP ==="
EMAIL="adapter-e2e-${UID_SUFFIX}@test.com"

REGISTER_RESP=$(curl -s -X POST "$AUTH_URL/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"businessName\":\"Adapter Test\",\"email\":\"$EMAIL\",\"password\":\"SecurePass123\",\"firstName\":\"Test\",\"lastName\":\"User\"}")
ACCESS_TOKEN=$(echo "$REGISTER_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")

# Extract businessId from JWT payload
BUSINESS_ID=$(echo "$ACCESS_TOKEN" | cut -d. -f2 | base64 -d 2>/dev/null | python3 -c "import sys,json; print(json.load(sys.stdin)['businessId'])")

KEY_RESP=$(curl -s -X POST "$AUTH_URL/api/v1/keys" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{"name":"Adapter E2E Key"}')
API_KEY=$(echo "$KEY_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin)['rawKey'])")

green "  Business ID: $BUSINESS_ID"
green "  API Key: ${API_KEY:0:20}...\n"

# ==============================================================================
bold "=== 1. HEALTH ==="
# ==============================================================================

RESPONSE=$(api GET "$ADAPTER_URL/health"); parse
assert_status "GET /health" "200" "$CODE"

# ==============================================================================
bold "\n=== 2. AUTH ENFORCEMENT ==="
# ==============================================================================

# No API key
RESPONSE=$(api POST "$ADAPTER_URL/api/v1/channels" \
  -H "Content-Type: application/json" \
  -d '{"businessId":"'$BUSINESS_ID'","provider":"WHATSAPP","displayName":"X","phoneNumber":"+1","phoneNumberId":"1","wabaId":"1","apiKey":"k"}')
parse
assert_status "Create channel without API key → 401" "401" "$CODE"

# Invalid API key
RESPONSE=$(api POST "$ADAPTER_URL/api/v1/channels" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: ba_live_invalidkey123" \
  -d '{"businessId":"'$BUSINESS_ID'","provider":"WHATSAPP","displayName":"X","phoneNumber":"+1","phoneNumberId":"1","wabaId":"1","apiKey":"k"}')
parse
assert_status "Create channel with invalid API key → 401" "401" "$CODE"

# No API key on messages
RESPONSE=$(api POST "$ADAPTER_URL/api/v1/messages/send" \
  -H "Content-Type: application/json" \
  -d '{"channelId":"fake","to":"123","type":"text","content":{"body":"hi"}}')
parse
assert_status "Send message without API key → 401" "401" "$CODE"

# ==============================================================================
bold "\n=== 3. CHANNEL CRUD ==="
# ==============================================================================

# Create channel
RESPONSE=$(api POST "$ADAPTER_URL/api/v1/channels" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $API_KEY" \
  -d "{\"businessId\":\"$BUSINESS_ID\",\"provider\":\"WHATSAPP\",\"displayName\":\"E2E WhatsApp\",\"phoneNumber\":\"+5511999990000\",\"phoneNumberId\":\"pn-${UID_SUFFIX}\",\"wabaId\":\"waba-${UID_SUFFIX}\",\"apiKey\":\"fake-meta-token\"}")
parse
assert_status "Create channel" "201" "$CODE"
CHANNEL_ID=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])")
WEBHOOK_TOKEN=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['webhookToken'])")
assert_json "Channel has displayName" "displayName" "E2E WhatsApp" "$BODY"

TOTAL=$((TOTAL + 1))
if [ -n "$WEBHOOK_TOKEN" ] && [ "$WEBHOOK_TOKEN" != "" ]; then
  green "  PASS: webhookToken returned on creation"
  PASSED=$((PASSED + 1))
else
  red "  FAIL: webhookToken not returned"
  FAILED=$((FAILED + 1))
fi

# Duplicate channel (same businessId + phoneNumberId)
RESPONSE=$(api POST "$ADAPTER_URL/api/v1/channels" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $API_KEY" \
  -d "{\"businessId\":\"$BUSINESS_ID\",\"provider\":\"WHATSAPP\",\"displayName\":\"Dupe\",\"phoneNumber\":\"+5511999990000\",\"phoneNumberId\":\"pn-${UID_SUFFIX}\",\"wabaId\":\"waba-${UID_SUFFIX}\",\"apiKey\":\"fake-token\"}")
parse
assert_status "Duplicate channel → 409" "409" "$CODE"

# Get channel
RESPONSE=$(api GET "$ADAPTER_URL/api/v1/channels/$CHANNEL_ID" \
  -H "X-API-Key: $API_KEY")
parse
assert_status "Get channel" "200" "$CODE"
assert_json "Get returns displayName" "displayName" "E2E WhatsApp" "$BODY"

# Verify webhookToken NOT in get response
TOTAL=$((TOTAL + 1))
HAS_TOKEN=$(echo "$BODY" | python3 -c "import sys,json; print('webhookToken' in json.load(sys.stdin))")
if [ "$HAS_TOKEN" = "False" ]; then
  green "  PASS: webhookToken not leaked in GET response"
  PASSED=$((PASSED + 1))
else
  red "  FAIL: webhookToken leaked in GET response"
  FAILED=$((FAILED + 1))
fi

# List channels for business
RESPONSE=$(api GET "$ADAPTER_URL/api/v1/channels/business/$BUSINESS_ID" \
  -H "X-API-Key: $API_KEY")
parse
assert_status "List channels" "200" "$CODE"

# Update channel
RESPONSE=$(api PATCH "$ADAPTER_URL/api/v1/channels/$CHANNEL_ID" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $API_KEY" \
  -d '{"displayName":"Updated WhatsApp"}')
parse
assert_status "Update channel" "200" "$CODE"
assert_json "Updated displayName" "displayName" "Updated WhatsApp" "$BODY"

# ==============================================================================
bold "\n=== 4. WEBHOOK VERIFICATION ==="
# ==============================================================================

# Valid verification
RESPONSE=$(api GET "$ADAPTER_URL/webhook/whatsapp/$WEBHOOK_TOKEN?hub.mode=subscribe&hub.challenge=test-challenge-123&hub.verify_token=local-dev-verify-token")
parse
assert_status "Webhook verify" "200" "$CODE"

TOTAL=$((TOTAL + 1))
if [ "$BODY" = "test-challenge-123" ]; then
  green "  PASS: Challenge echoed correctly"
  PASSED=$((PASSED + 1))
else
  red "  FAIL: Challenge not echoed (got: $BODY)"
  FAILED=$((FAILED + 1))
fi

# Wrong verify token
RESPONSE=$(api GET "$ADAPTER_URL/webhook/whatsapp/$WEBHOOK_TOKEN?hub.mode=subscribe&hub.challenge=test&hub.verify_token=wrong-token")
parse
assert_status "Webhook verify wrong token → 401" "401" "$CODE"

# Unknown webhook token
RESPONSE=$(api GET "$ADAPTER_URL/webhook/whatsapp/unknown-token?hub.mode=subscribe&hub.challenge=test&hub.verify_token=local-dev-verify-token")
parse
assert_status "Webhook verify unknown token → 401" "401" "$CODE"

# ==============================================================================
bold "\n=== 5. WEBHOOK MESSAGE INGESTION ==="
# ==============================================================================

# Build a valid Meta webhook payload
WEBHOOK_BODY='{
  "object": "whatsapp_business_account",
  "entry": [{
    "id": "waba-123",
    "changes": [{
      "field": "messages",
      "value": {
        "messaging_product": "whatsapp",
        "metadata": {
          "display_phone_number": "+5511999990000",
          "phone_number_id": "pn-'$UID_SUFFIX'"
        },
        "contacts": [{"profile": {"name": "Customer"}, "wa_id": "5511888880000"}],
        "messages": [{
          "from": "5511888880000",
          "id": "wamid.test123",
          "timestamp": "1711100000",
          "type": "text",
          "text": {"body": "Hello, I need help!"}
        }]
      }
    }]
  }]
}'

# Compute HMAC-SHA256 signature
SIGNATURE=$(echo -n "$WEBHOOK_BODY" | openssl dgst -sha256 -hmac "local-dev-meta-app-secret" | awk '{print $2}')

# Post webhook with valid signature (inbox-service is not running, so forwarding will fail silently — that's OK)
RESPONSE=$(api POST "$ADAPTER_URL/webhook/whatsapp/$WEBHOOK_TOKEN" \
  -H "Content-Type: application/json" \
  -H "X-Hub-Signature-256: sha256=$SIGNATURE" \
  -d "$WEBHOOK_BODY")
parse
assert_status "Webhook with valid signature → 200" "200" "$CODE"

# Post with invalid signature
RESPONSE=$(api POST "$ADAPTER_URL/webhook/whatsapp/$WEBHOOK_TOKEN" \
  -H "Content-Type: application/json" \
  -H "X-Hub-Signature-256: sha256=invalidsignature" \
  -d "$WEBHOOK_BODY")
parse
assert_status "Webhook with invalid signature → 401" "401" "$CODE"

# Post without signature
RESPONSE=$(api POST "$ADAPTER_URL/webhook/whatsapp/$WEBHOOK_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$WEBHOOK_BODY")
parse
assert_status "Webhook without signature → 401" "401" "$CODE"

# ==============================================================================
bold "\n=== 6. INSTAGRAM CHANNEL ==="
# ==============================================================================

# Instagram channel without required fields
RESPONSE=$(api POST "$ADAPTER_URL/api/v1/channels" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $API_KEY" \
  -d "{\"businessId\":\"$BUSINESS_ID\",\"provider\":\"INSTAGRAM\",\"displayName\":\"Bad IG\",\"apiKey\":\"token\"}")
parse
assert_status "Instagram channel without required fields → 400" "400" "$CODE"

# Create Instagram channel
RESPONSE=$(api POST "$ADAPTER_URL/api/v1/channels" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $API_KEY" \
  -d "{\"businessId\":\"$BUSINESS_ID\",\"provider\":\"INSTAGRAM\",\"displayName\":\"E2E Instagram\",\"pageId\":\"page-${UID_SUFFIX}\",\"instagramAccountId\":\"ig-${UID_SUFFIX}\",\"apiKey\":\"fake-ig-token\"}")
parse
assert_status "Create Instagram channel" "201" "$CODE"
IG_CHANNEL_ID=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])")
IG_WEBHOOK_TOKEN=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['webhookToken'])")

# Verify Instagram webhook
RESPONSE=$(api GET "$ADAPTER_URL/webhook/instagram/$IG_WEBHOOK_TOKEN?hub.mode=subscribe&hub.challenge=ig-challenge&hub.verify_token=local-dev-verify-token")
parse
assert_status "Instagram webhook verify" "200" "$CODE"

TOTAL=$((TOTAL + 1))
if [ "$BODY" = "ig-challenge" ]; then
  green "  PASS: Instagram challenge echoed correctly"
  PASSED=$((PASSED + 1))
else
  red "  FAIL: Instagram challenge not echoed (got: $BODY)"
  FAILED=$((FAILED + 1))
fi

# Deactivate Instagram channel
RESPONSE=$(api DELETE "$ADAPTER_URL/api/v1/channels/$IG_CHANNEL_ID" \
  -H "X-API-Key: $API_KEY")
parse
assert_status "Deactivate Instagram channel" "204" "$CODE"

# ==============================================================================
bold "\n=== 7. CHANNEL DEACTIVATION ==="
# ==============================================================================

RESPONSE=$(api DELETE "$ADAPTER_URL/api/v1/channels/$CHANNEL_ID" \
  -H "X-API-Key: $API_KEY")
parse
assert_status "Deactivate channel" "204" "$CODE"

# Verify deactivated (list should be empty)
RESPONSE=$(api GET "$ADAPTER_URL/api/v1/channels/business/$BUSINESS_ID" \
  -H "X-API-Key: $API_KEY")
parse
assert_status "List after deactivation" "200" "$CODE"

TOTAL=$((TOTAL + 1))
COUNT=$(echo "$BODY" | python3 -c "import sys,json; print(len(json.load(sys.stdin)))")
if [ "$COUNT" = "0" ]; then
  green "  PASS: No active channels after deactivation"
  PASSED=$((PASSED + 1))
else
  red "  FAIL: Expected 0 active channels, got $COUNT"
  FAILED=$((FAILED + 1))
fi

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
