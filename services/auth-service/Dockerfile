# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /project

COPY gradlew gradlew.bat gradle.properties ./
COPY gradle/ gradle/
COPY settings.gradle.kts ./
COPY app/build.gradle.kts app/build.gradle.kts

RUN ./gradlew :app:dependencies --no-daemon 2>/dev/null || true

COPY app/src/ app/src/

RUN ./gradlew :app:bootJar --no-daemon

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre

RUN groupadd --system appuser && useradd --system --gid appuser appuser

WORKDIR /app

COPY --from=build /project/app/build/libs/*.jar app.jar

RUN chown -R appuser:appuser /app
USER appuser

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
