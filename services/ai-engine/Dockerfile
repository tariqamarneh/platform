# ---- Build stage ----
FROM python:3.13-slim AS build

WORKDIR /app

RUN pip install poetry && poetry config virtualenvs.in-project true

COPY pyproject.toml poetry.lock ./
RUN poetry install --no-root --no-interaction

COPY . .

# ---- Runtime stage ----
FROM python:3.13-slim

RUN groupadd --system appuser && useradd --system --gid appuser appuser

WORKDIR /app

COPY --from=build /app/.venv ./.venv
COPY --from=build /app/ai_engine ./ai_engine

ENV PATH="/app/.venv/bin:$PATH"
ENV PORT=8083

USER appuser

EXPOSE 8083

CMD ["python", "-m", "ai_engine.main"]
