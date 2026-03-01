# Build stage: Kotlin/Spring Boot backend (Gradle 8.5, JDK 21)
FROM gradle:8.5-jdk21-alpine AS builder

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle

# Download deps (cached unless build files change)
RUN gradle dependencies --no-daemon || true

COPY src src
RUN gradle bootJar --no-daemon

# Run stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Non-root user
RUN adduser -D -g "" appuser

# Single JAR from build
COPY --from=builder /app/build/libs/*.jar app.jar

USER appuser

# Production: use prod profile (Neon + CORS for Firebase). Set SPRING_DATASOURCE_PASSWORD in Cloud Run.
ENV SPRING_PROFILES_ACTIVE=prod

# Cloud Run sets PORT (default 8080); Spring Boot reads it via server.port=${PORT:5173}
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
