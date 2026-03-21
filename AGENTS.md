# Platform Monorepo Agent Rules

## Repository Structure
This is a monorepo with three independent microservices under `services/`. Each service has its own language, build tool, and deployment pipeline. Do not assume shared dependencies or build steps across services.

## Service Boundaries
- **auth-service**: Java 21 with Gradle. Source lives in `app/src/main/java/com/businessagent/auth/`.
- **ai-engine**: Python 3.13 with Poetry. Source lives in `ai_engine/`.
- **web**: Next.js 16 with npm. See `services/web/AGENTS.md` for Next.js-specific rules.

## Key Conventions
- Each service is independently buildable and deployable — never introduce cross-service build dependencies.
- Each service will have its own Dockerfile targeting Google Cloud Run.
- When working on a single service, `cd` into its directory first. Do not modify files outside the target service unless explicitly asked.
- The root `.gitignore` covers all three ecosystems. Each service may also have its own `.gitignore` for service-specific patterns.
