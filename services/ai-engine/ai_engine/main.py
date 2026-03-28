import logging
import os

from fastapi import FastAPI
import uvicorn

from ai_engine.router.process import router as process_router

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(name)s] %(levelname)s - %(message)s",
)

app = FastAPI(
    title="AI Engine",
    description="AI processing service for the Business Agent platform (mock mode)",
    version="0.1.0",
)

app.include_router(process_router)


@app.get("/health")
def health():
    return {"status": "ok", "mode": "mock"}


@app.get("/")
def root():
    return {"service": "ai-engine", "mode": "mock"}


if __name__ == "__main__":
    port = int(os.environ.get("PORT", "8083"))
    uvicorn.run(app, host="0.0.0.0", port=port)
