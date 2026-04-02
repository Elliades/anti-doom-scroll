# AGENTS.md

## Cursor Cloud specific instructions

### Overview

Anti-Doom Scroll is a Kotlin/Spring Boot 3 + React/TypeScript/Vite PWA. Two services run locally:

| Service | Port | Start command |
|---------|------|---------------|
| Backend (Kotlin, H2 in-memory) | 5173 | `./gradlew bootRun --args='--spring.profiles.active=local'` |
| Frontend (Vite dev server) | 5174 | `cd frontend && npm run dev` |

No PostgreSQL, Docker, or external services needed for local development — the `local` profile uses embedded H2.

### Key caveats

- **`gradlew` (unix):** The repo only ships `gradlew.bat` (Windows). The update script generates the unix wrapper via `gradle wrapper` if missing. If `./gradlew` doesn't exist, run: `java -classpath gradle/wrapper/gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain wrapper`
- **`tsc -b` in frontend:** `npm run build` runs `tsc -b && vite build`. The `tsc -b` step has a pre-existing type error in `vite.config.ts` (vitest `test` property not recognized by tsconfig.node.json). The actual Vite build (`npx vite build`) works fine. This does not affect `npm run dev` or `npm run test`.
- **Backend port 5173:** The Spring Boot backend binds to port 5173 (not the default 8080). The frontend proxies `/api` requests to `http://localhost:5173`.

### Running tests

- Backend: `./gradlew test` (uses H2 in-memory with `test` profile, 210 tests)
- Frontend: `cd frontend && npm run test` (vitest, 6 tests)

### Lint

No dedicated lint command is configured. TypeScript checking: `cd frontend && npx tsc --noEmit` (has pre-existing issues in vite.config.ts). Backend compilation check: `./gradlew compileKotlin`.

### Build

- Backend: `./gradlew bootJar`
- Frontend: `cd frontend && npx vite build` (skip `tsc -b` to avoid pre-existing type error)

### Project structure

See `README.md` for architecture details and `.cursorrules` for exercise/ladder/subject conventions.
