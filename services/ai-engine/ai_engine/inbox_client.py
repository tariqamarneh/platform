import httpx
import logging

from ai_engine.config import INBOX_SERVICE_URL

logger = logging.getLogger(__name__)

_client = httpx.Client(base_url=INBOX_SERVICE_URL, timeout=10.0)


def set_typing(conversation_id: str, api_key: str) -> None:
    try:
        _client.post(
            "/api/v1/typing",
            json={"conversationId": conversation_id},
            headers={"X-API-Key": api_key, "Content-Type": "application/json"},
        )
        logger.info("Typing indicator sent: conversation=%s", conversation_id)
    except Exception as e:
        logger.warning("Failed to set typing: %s", e)


def get_messages(conversation_id: str, api_key: str) -> list[dict]:
    try:
        resp = _client.get(
            f"/api/v1/conversations/{conversation_id}/messages",
            params={"page": 0, "size": 20},
            headers={"X-API-Key": api_key},
        )
        if resp.status_code == 200:
            data = resp.json()
            return data.get("content", [])
        logger.warning("Failed to get messages: HTTP %d", resp.status_code)
        return []
    except Exception as e:
        logger.warning("Failed to get messages: %s", e)
        return []


def send_reply(conversation_id: str, business_id: str, message: str, api_key: str) -> dict | None:
    try:
        resp = _client.post(
            "/api/v1/reply",
            json={
                "conversationId": conversation_id,
                "businessId": business_id,
                "messageType": "text",
                "content": {"body": message},
            },
            headers={"X-API-Key": api_key, "Content-Type": "application/json"},
        )
        if resp.status_code == 200:
            result = resp.json()
            logger.info("Reply sent: messageId=%s", result.get("messageId"))
            return result
        logger.warning("Failed to send reply: HTTP %d — %s", resp.status_code, resp.text)
        return None
    except Exception as e:
        logger.warning("Failed to send reply: %s", e)
        return None
