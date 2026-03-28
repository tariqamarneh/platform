import logging
from typing import Any

from fastapi import APIRouter, BackgroundTasks
from pydantic import BaseModel

from ai_engine import inbox_client, mock_ai

logger = logging.getLogger(__name__)

router = APIRouter()


class ProcessRequest(BaseModel):
    conversationId: str
    businessId: str
    messageId: str
    contactId: str
    contactName: str = ""
    message: str
    messageType: str
    apiKey: str = ""  # passed by inbox-service for downstream calls


@router.post("/api/v1/process")
async def process_message(request: ProcessRequest, background_tasks: BackgroundTasks):
    """
    Called by inbox-service when a new inbound message arrives.
    Processes the message asynchronously — sets typing, gets history,
    generates a response, and sends the reply.
    """
    logger.info(
        "Received message: conversation=%s, business=%s, type=%s",
        request.conversationId,
        request.businessId,
        request.messageType,
    )

    background_tasks.add_task(handle_message, request)

    return {"status": "processing"}


def handle_message(request: ProcessRequest):
    """Background task that processes the message and sends a reply."""
    api_key = request.apiKey
    conversation_id = request.conversationId
    business_id = request.businessId

    try:
        # 1. Set typing indicator
        if api_key:
            inbox_client.set_typing(conversation_id, api_key)

        # 2. Get conversation history
        history = []
        if api_key:
            history = inbox_client.get_messages(conversation_id, api_key)

        # 3. Generate AI response (mock)
        response_text = mock_ai.generate_response(request.message, history)

        logger.info(
            "Generated response: conversation=%s, response=%s",
            conversation_id,
            response_text[:80],
        )

        # 4. Send reply via inbox-service
        if api_key:
            inbox_client.send_reply(conversation_id, business_id, response_text, api_key)
        else:
            logger.warning("No API key provided, cannot send reply")

    except Exception as e:
        logger.error(
            "Failed to process message: conversation=%s, error=%s",
            conversation_id,
            str(e),
        )
