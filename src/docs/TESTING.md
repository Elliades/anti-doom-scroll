# Testing

This document describes integration/functional tests and how to run them.

## Overview

- **Unit tests**: domain and use cases with mocked ports (e.g. `StartSessionUseCaseTest`).
- **Integration tests**: full Spring context, in-memory H2, MockMvc against REST API (session, subjects, journey, openapp).

## Test configuration

- Profile: `test` (`@ActiveProfiles("test")`).
- Database: H2 in-memory, PostgreSQL compatibility mode; Flyway disabled; JPA `ddl-auto: create-drop`.
- Data: `src/test/resources/data.sql` (subjects, exercises).
- Journey: `src/test/resources/application-test.yml` defines `app.journey` (OPEN_APP, REFLECTION, CHAPTER_EXERCISES).

## Integration / functional test classes

| Test class | What it tests |
|------------|----------------|
| **SessionApiIntegrationTest** | Health, `GET /api/session/start`, `GET /api/subjects`. |
| **NBackSessionApiIntegrationTest** | Session with `preferType=N_BACK`; N-back exercise shape. |
| **OpenAppSessionApiIntegrationTest** | `GET /api/session/start?mode=openapp`: up to 3 steps, ULTRA_EASY/EASY only; step shape; default session unchanged without mode. |
| **JourneyApiIntegrationTest** | `GET /api/journey` (definition, 404 for unknown code); `GET /api/journey/steps/{stepIndex}/content` for OPEN_APP (session), REFLECTION (title/body), CHAPTER_EXERCISES (chapters + session); chapterIndex; 404 for out-of-range step; profileId. |

## Unit test classes

| Test class | What it tests |
|------------|----------------|
| **StartSessionUseCaseTest** | `startOpenAppSession`: 3 steps, fewer steps, empty steps; verifies `findRandomUltraEasyOrEasy(3)` called. |
| **NBackParamsTest** | Domain N-back params parsing. |

## Running tests

From project root:

```bash
./gradlew test          # Unix/macOS
gradlew.bat test        # Windows
```

If the Gradle wrapper is missing, generate it with `gradle wrapper` (requires Gradle on PATH), or run tests from your IDE (e.g. run `JourneyApiIntegrationTest` or the whole `src/test` tree).

## Adding tests

- **New API endpoint**: add integration test in the appropriate `*ApiIntegrationTest` class (or create one) using MockMvc and `@Sql` if extra data is needed.
- **New use case**: add unit test with mocked ports (`@Mock`, `@ExtendWith(MockitoExtension::class)`).
- **New journey step type**: extend `JourneyApiIntegrationTest` and, if needed, add step/config in `application-test.yml`.
