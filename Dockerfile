# ── Stage 1: Build frontend ──────────────────────────────────────────
FROM node:20-alpine AS frontend

WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci

COPY frontend/ ./
RUN npm run build

# ── Stage 2: Build backend (includes frontend static files) ─────────
FROM gradle:8.5-jdk21-alpine AS backend

WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle
RUN gradle dependencies --no-daemon || true

COPY src src
COPY --from=frontend /app/frontend/dist/ src/main/resources/static/
RUN gradle bootJar --no-daemon

# ── Stage 3: Runtime ─────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
RUN adduser -D -g "" appuser

COPY --from=backend /app/build/libs/*.jar app.jar

USER appuser

ENV SPRING_PROFILES_ACTIVE=railway
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
