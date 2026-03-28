import os


INBOX_SERVICE_URL = os.environ.get("INBOX_SERVICE_URL", "http://localhost:8082")
PORT = int(os.environ.get("PORT", "8083"))
