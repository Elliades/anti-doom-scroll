# ── Stage 1: Build frontend ──────────────────────────────────────────
FROM node:20-alpine AS frontend

WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci

COPY frontend/ ./

# Repo .env.production may target Firebase + Cloud Run. For this image the SPA is
# served from the Spring Boot jar, so use same-origin /api (empty overrides file).
ENV VITE_API_URL=
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
RUN apk add --no-cache wget \
  && adduser -D -g "" appuser

COPY --from=backend /app/build/libs/*.jar app.jar

USER appuser

# Default Cloud Run profile; Railway can set SPRING_PROFILES_ACTIVE=railway + secrets in the service.
ENV SPRING_PROFILES_ACTIVE=prod

# Cloud Run may set PORT; Spring should read server.port accordingly in config.
EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseContainerSupport", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
