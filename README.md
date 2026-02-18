# Anti-Doom Scroll

JITAI-like app that replaces doomscroll with micro-exercises: **ultra-easy → easy → medium → optional hard**. Fast reward, real skill.

## Tech stack

- **Backend:** Kotlin 1.9, Java 21, Spring Boot 3, Gradle, PostgreSQL, Flyway
- **API:** REST JSON
- **Frontend:** React, TypeScript, Vite, PWA (installable)
- **Auth:** MVP = anonymous local profile (no account)

## Architecture

- **domain** — pure entities (Exercise, Attempt, UserProfile, DailyPlan, Card, etc.)
- **application** — use cases and ports (e.g. `StartSessionUseCase`, `ExercisePort`)
- **infrastructure** — JPA, Flyway, adapters
- **web** — REST controllers, DTOs (no business logic in controllers)

## Prerequisites

- **JDK 21**
- **Gradle 8+** (or generate wrapper: `gradle wrapper`, then use `./gradlew` / `gradlew.bat`)
- **Node 18+** (for frontend)
- **PostgreSQL** (for backend; optional for quick run see below)

## Backend

### With PostgreSQL

1. Create DB and user:

```bash
createdb antidoomscroll
createuser -P antidoomscroll  # password: antidoomscroll (or set in application.yml)
```

2. Run:

```bash
./gradlew bootRun
```

API: http://localhost:8080  
- `GET /api/health` — health  
- `GET /api/session/start` — start session (ultra-easy step cached for &lt;1s reopen)  
- `GET /api/journey?code=default` — journey definition (steps)  
- `GET /api/journey/steps/{stepIndex}/content` — step content (open-app session, reflection, or chapter series)

### Without PostgreSQL (tests only)

```bash
./gradlew test
```

Uses H2 in-memory with `test` profile.

## Frontend

```bash
cd frontend
npm install
npm run dev
```

App: http://localhost:5173  
- Proxies `/api` to http://localhost:8080  
- **Journey flow:** on load, fetches journey then step 0 (ultra-easy exercises) → step 1 (“Why am I doom scrolling?”) → step 2 (exercises by chapter). You can jump to any step via the step nav or URL `?step=0|1|2`.

### PWA

- `npm run build` then `npm run preview` to test production build.
- Add `public/icon-192.png` and `public/icon-512.png` for install icons (optional).

## Config (backend)

- `application.yml`: DB URL, JPA, Flyway, `app.session-default-seconds`, `app.low-battery-mode-seconds`, `app.anonymous-profile-enabled`.
- **Journey:** `app.journey` defines the default journey (steps: OPEN_APP, REFLECTION, CHAPTER_EXERCISES) and reflection content. See `src/docs/architecture-journey.md`. Add or reorder steps in YAML to scale.
- CORS: `app.cors.origins` (default `http://localhost:5173`).

## Product rules (from .cursorrules)

- Reopen launches an ultra-easy exercise in **&lt;1 second** (cached).
- Session: step 1 ultra-easy, step 2 easy/medium by axis, step 3 optional hard.
- Default session 2–5 min; “low battery brain” mode 30–60 s.
- Scoring: transparent, no harsh punishment; deterministic in MVP.

## License

Private / unlicensed unless stated otherwise.
