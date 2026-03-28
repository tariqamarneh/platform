"""
Mock AI response generator.

Replace this module with real AI (Claude, OpenAI, etc.) when ready.
For now, generates simple canned responses based on the message content.
"""

import random

GREETINGS = {"hi", "hello", "hey", "hola", "salam", "marhaba", "good morning", "good evening"}

CANNED_RESPONSES = [
    "Thank you for your message! Our team is looking into this.",
    "I understand. Let me help you with that.",
    "Great question! Here's what I can tell you...",
    "I appreciate you reaching out. How else can I assist you?",
    "That's a great point. Let me check on that for you.",
]


def generate_response(message: str, history: list[dict] | None = None) -> str:
    """Generate a mock AI response based on the incoming message."""
    lower = message.lower().strip()

    # Greeting
    if any(g in lower for g in GREETINGS):
        return "Hello! Welcome to our support. How can I help you today?"

    # Thank you
    if "thank" in lower or "thanks" in lower:
        return "You're welcome! Is there anything else I can help with?"

    # Help
    if "help" in lower or "support" in lower:
        return "I'm here to help! Could you please describe what you need assistance with?"

    # Pricing
    if "price" in lower or "cost" in lower or "pricing" in lower:
        return "We offer a Free plan (100 messages/day) and a Pro plan ($49/month) with unlimited messages. Would you like more details?"

    # Hours
    if "hour" in lower or "open" in lower or "available" in lower:
        return "Our AI support is available 24/7! For human assistance, our team is available Monday-Friday, 9am-6pm."

    # Default: echo with context
    msg_count = len(history) if history else 0
    if msg_count > 3:
        return f"I've been following our conversation. Regarding \"{message[:50]}\" — let me look into that for you."

    return random.choice(CANNED_RESPONSES)
