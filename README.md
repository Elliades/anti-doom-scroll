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

## Quick start (recommended for testing)

To get subjects and exercises **without** PostgreSQL:

1. **Start backend** (Terminal 1):  
   `.\scripts\start-backend-local.ps1`  
   Or: `.\gradlew.bat bootRun --args='--spring.profiles.active=local'`
2. **Start frontend** (Terminal 2):  
   `.\scripts\start-frontend.ps1`  
   Or: `cd frontend` → `npm install` → `npm run dev`
3. Open **http://localhost:5174** in your browser. (If 5174 is in use, Vite will use 5175, 5176, etc.—check the terminal output.)

Use **Subjects** or **All exercises** to browse and play.

If `/api/subjects` and `/api/exercises` return `[]`, the backend is running without the local profile. Restart with the command above.

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

API: http://localhost:5173  
- `GET /api/health` — health  
- `GET /api/session/start` — start session (ultra-easy step cached for &lt;1s reopen)  
- `GET /api/journey?code=default` — journey definition (steps)  
- `GET /api/journey/steps/{stepIndex}/content` — step content (open-app session, reflection, or chapter series)
- `GET /api/subjects` — list all subjects  
- `GET /api/subjects/{code}` — get one subject by code  
- `GET /api/subjects/{code}/exercises` — list exercises for that subject  
- `GET /api/exercises/{id}` — get one exercise by ID (for dedicated play)  
- `GET /api/nback/{level}` — N-back exercise by level (1, 2, 3)

### Without Docker / without PostgreSQL (local profile)

To run the backend with no Docker and no PostgreSQL (in-memory H2):

**Windows (recommended — frees port 5173 first):**
```powershell
.\scripts\start-backend-local.ps1
```

**Or manually:**
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```
Windows: `.\gradlew.bat bootRun --args='--spring.profiles.active=local'`.  
If port 5173 is in use: `.\scripts\free-port-5173.ps1 -Kill` then rerun.

API: http://localhost:5173 (same endpoints as above). With the local profile, subjects and exercises are seeded on startup so `GET /api/subjects` and `GET /api/exercises` return data. Data is in-memory and reset on each restart. For persistent data, use PostgreSQL (see above).

### Without PostgreSQL (tests only)

```bash
./gradlew test
```

Uses H2 in-memory with `test` profile.

### Port 5173 already in use

If `./gradlew bootRun` fails with **"Port 5173 was already in use"**, something else is bound to 5173—often a previous backend instance, or another app. Options:

- **Windows (PowerShell):**  
  See what’s using 5173: `Get-NetTCPConnection -LocalPort 5173 -ErrorAction SilentlyContinue | Select-Object OwningProcess`.  
  Then stop the process: `Stop-Process -Id <PID> -Force` (replace `<PID>` with the number from OwningProcess).  
  Or run `.\scripts\free-port-5173.ps1` to list and optionally kill the process.
- **Linux/macOS:**  
  `lsof -i :5173` then `kill <PID>`, or `kill $(lsof -t -i :5173)`.

2. **Use another port** (e.g. if 5173 is used by another app): run `./gradlew bootRun --args='--server.port=5175'`, then in `frontend/vite.config.ts` set the proxy `target` to `http://localhost:5175`.

### bootRun fails with "Process finished with non-zero exit value 1"

Gradle only reports that the JVM exited with code 1; the **real error** is in the application startup log. Do this:

1. **See the full error** by running the built JAR in the foreground (same as bootRun, but one process so logs are clear):

   ```bash
   ./gradlew bootJar
   java -jar build/libs/anti-doom-scroll-0.0.1-SNAPSHOT.jar
   ```

   Or use the shortcut: `./gradlew runJar` (Windows: `.\gradlew.bat runJar`). The console will show the Spring Boot exception (e.g. database connection, port in use, schema mismatch).

2. **Common causes:**
   - **PostgreSQL not running or not reachable** — start PostgreSQL and ensure `localhost:5432` is reachable; create DB and user (see "With PostgreSQL" above).
   - **Port 5173 in use** — free the port or use `--server.port=5175` (see "Port 5173 already in use").
   - **Database schema mismatch** — if you see "wrong column type" for `exercise_params`, ensure migrations are applied and the entity matches the DB (e.g. jsonb column).

3. **Capture logs to a file:**  
   `./gradlew bootRun --no-daemon 2>&1 | tee run.log` then open `run.log` and search for `ERROR` or `Exception`.

### Build fails with "Input/output error" or compileKotlin I/O error

Usually a bad Gradle/cache state or environment (Docker, WSL, antivirus). Try in order:

1. **Clean and rebuild without daemon** (stops Gradle daemons and clears build output):

   ```bash
   ./gradlew --stop
   ./gradlew clean
   ./gradlew compileKotlin --no-daemon
   ./gradlew bootRun --no-daemon
   ```

   On Windows: `gradlew.bat` instead of `./gradlew`.

2. **Full reset** (if still failing): remove local build and Gradle caches, then rebuild:

   ```bash
   ./gradlew --stop
   rm -rf build .gradle
   ./gradlew bootRun --no-daemon
   ```

   Windows PowerShell: `Remove-Item -Recurse -Force build, .gradle -ErrorAction SilentlyContinue` then `.\gradlew.bat bootRun --no-daemon`.

3. **Environment**: If running in Docker/WSL, ensure the project is on a normal disk (not a slow or locked network mount). Temporarily exclude the project or Gradle cache from antivirus if it scans on access.

## Frontend

```bash
cd frontend
npm install
npm run dev
```

App: http://localhost:5174  
- Proxies `/api` to http://localhost:5173  
- **Journey flow:** `/` — journey steps (ultra-easy → reflection → chapter exercises); step nav or URL `?step=0|1|2`.  
- **Subjects:** `/subjects` — list subjects; `/subjects/:code` — exercises for that subject; `/play/:exerciseId` — play one exercise (same component as in journey). See `src/docs/architecture-scaling.md`.

### PWA

- `npm run build` then `npm run preview` to test production build.
- Add `public/icon-192.png` and `public/icon-512.png` for install icons (optional).

### Deploy frontend to Firebase Hosting

The frontend (React + Vite) can be deployed as a static site to [Firebase Hosting](https://firebase.google.com/docs/hosting). The backend is not deployed by this flow; you need to run it elsewhere (e.g. Cloud Run, Railway) and point the app at it (env or rewrites).

**One-time setup**

1. Install Firebase CLI: `npm install -g firebase-tools`
2. Log in: `firebase login`
3. Create a project in [Firebase Console](https://console.firebase.google.com/) (or use an existing one).
4. Link this repo: from project root run `firebase use --add` and select the project.

**Deploy**

From **project root**: `.\scripts\deploy-firebase.ps1`

From **frontend** directory: `npm run deploy`

Or manually: `cd frontend` → `npm run build` → `cd ..` → `firebase deploy`.

After deploy, the Hosting URL (e.g. `https://<project>.web.app`) is shown in the CLI. The app will load; API calls will show a clear "Backend not available" message until you deploy the backend. Then set **`VITE_API_URL`** to your backend base URL including `/api` (e.g. `https://your-backend.run.app/api`) and rebuild/redeploy the frontend so the app calls your backend instead of the same origin.

### Deploy backend to Google Cloud Run

The backend runs as a container on [Cloud Run](https://cloud.google.com/run). The repo includes a **Dockerfile at the root** (multi-stage: Gradle build → JRE + JAR) and a deploy script.

**Why this Dockerfile (not a generic one):**

- The project has no Unix `gradlew` (only `gradlew.bat`), so the build stage uses the official `gradle:8.5-jdk21-alpine` image and runs `gradle bootJar` instead of `./gradlew`.
- Multi-stage build keeps the final image small (only the JAR + JRE).
- `.dockerignore` excludes frontend, IDE files, and caches so the build context is small.
- The app listens on **PORT** (Cloud Run sets `PORT=8080`); `application.yml` uses `server.port: ${PORT:5173}` so local dev stays on 5173.

**One-time setup**

1. Install [Google Cloud SDK](https://cloud.google.com/sdk/docs/install) and log in: `gcloud auth login`
2. Set project: `gcloud config set project YOUR_PROJECT_ID`
3. Enable APIs: `gcloud services enable run.googleapis.com artifactregistry.googleapis.com`
4. Have a PostgreSQL instance (e.g. [Cloud SQL](https://cloud.google.com/sql) or [Neon](https://neon.tech)). For Neon, use the `neon` profile and set the DB URL via env (see below).

**Deploy**

From **project root**:

```powershell
.\scripts\deploy-backend-cloudrun.ps1
```

Optional: `-ServiceName "my-api" -Region "europe-west1" -ProjectId "my-gcp-project"`

The script runs `gcloud run deploy --source .`, which builds the image in Cloud Build from the root Dockerfile and deploys the service. The first run may take several minutes.

**Configure the service (required for DB)**

In [Cloud Run console](https://console.cloud.google.com/run) → your service → **Edit & deploy new revision** → **Variables & secrets**:

- `SPRING_PROFILES_ACTIVE` = `neon` (or leave unset if you use another profile with env-based DB config)
- `SPRING_DATASOURCE_URL` = your JDBC URL (e.g. Neon: `jdbc:postgresql://ep-xxx-pooler.region.aws.neon.tech/neondb?sslmode=require`)
- `SPRING_DATASOURCE_USERNAME` = your DB user
- `SPRING_DATASOURCE_PASSWORD` = your DB password (prefer [Secret Manager](https://cloud.google.com/run/docs/configuring/secrets) for production)
- `APP_CORS_ORIGINS` = your frontend origins, comma-separated (e.g. `https://your-app.web.app,https://your-domain.com`)

Redeploy after changing variables. Then set your frontend **`VITE_API_URL`** to `https://YOUR_SERVICE_URL/api` and rebuild/redeploy the frontend.

### Production (prod branch): Cloud Run + Neon + Firebase

On the **`prod`** branch the app is wired for production:

- **Backend:** Cloud Run. Production config is in **`application-prod.yml`** (Neon URL, username, CORS for Firebase). The Docker image defaults to `SPRING_PROFILES_ACTIVE=prod`. The only value you set in Cloud Run is **`SPRING_DATASOURCE_PASSWORD`** (once).
- **Database:** Neon PostgreSQL (URL and user are in `application-prod.yml`).
- **Frontend:** Firebase Hosting; `frontend/.env.production` sets `VITE_API_URL` to the Cloud Run API.

**Deploy backend** (do this every time you deploy; the script sets the DB password on the service then builds and deploys the image so the new revision starts successfully):

```powershell
$env:NEON_DB_PASSWORD = "your-neon-password"
.\scripts\deploy-backend-cloudrun.ps1
```

**Deploy frontend:**

```powershell
.\scripts\deploy-production.ps1
```

## Config (backend)

- `application.yml`: DB URL, JPA, Flyway, `app.session-default-seconds`, `app.low-battery-mode-seconds`, `app.anonymous-profile-enabled`.
- **Journey:** `app.journey` defines the default journey (steps: OPEN_APP, REFLECTION, CHAPTER_EXERCISES) and reflection content. See `src/docs/architecture-journey.md`. Add or reorder steps in YAML to scale.
- CORS: `app.cors.origins` (default includes `http://localhost:5173`, `http://localhost:5174`).

## Product rules (from .cursorrules)

- Reopen launches an ultra-easy exercise in **&lt;1 second** (cached).
- Session: step 1 ultra-easy, step 2 easy/medium by axis, step 3 optional hard.
- Default session 2–5 min; “low battery brain” mode 30–60 s.
- Scoring: transparent, no harsh punishment; deterministic in MVP.

## License

Private / unlicensed unless stated otherwise.
