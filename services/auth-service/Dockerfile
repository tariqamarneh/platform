# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS build

WORKDIR /project

# Copy Gradle wrapper and build scripts first (layer caching)
COPY gradlew gradlew.bat gradle.properties ./
COPY gradle/ gradle/
COPY settings.gradle.kts ./
COPY app/build.gradle.kts app/build.gradle.kts

# Download dependencies (cached unless build files change)
RUN ./gradlew :app:dependencies --no-daemon 2>/dev/null || true

# Copy application source
COPY app/src/ app/src/

# Build the distribution
RUN ./gradlew :app:installDist --no-daemon

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre

RUN groupadd --system appuser && useradd --system --gid appuser appuser

WORKDIR /app

COPY --from=build /project/app/build/install/app/ ./

RUN chown -R appuser:appuser /app
USER appuser

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["./bin/app"]
