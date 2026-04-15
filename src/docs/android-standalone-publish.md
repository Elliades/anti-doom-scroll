# Android / phone: standalone install & checks

“**Standalone**” here means:

1. **PWA:** `manifest` uses `display: standalone` (full-screen, no browser chrome) — see `frontend/vite.config.ts` (VitePWA).
2. **Capacitor:** the same web build is wrapped in the Android app (`frontend/android/`) and loads from bundled assets.

## Same repo as the web app? (coexistence)

**Yes — keep these changes on `main` (or your usual integration branch).** You do **not** need a dedicated Android-only branch for normal work.

| Surface | How it behaves |
|--------|----------------|
| **Local web dev** (`npm run dev` in `frontend/`) | Uses Vite’s default env (no `VITE_DATA_MODE=local` unless you add a `.env` that sets it). The app talks to the API via the `/api` proxy to Spring Boot on **5173**. Offline JSON under `public/data/` is **ignored** unless you opt in. |
| **Production web build** (`npm run build`) | Same unless you set `VITE_DATA_MODE` / `VITE_API_URL` in the build environment. Hosted Firebase (or similar) typically sets `VITE_API_URL` to your API. |
| **Android build** (`npm run build:android`) | Reads **`frontend/.env.android`** only for this mode. `VITE_DATA_MODE=local` bundles JSON; `remote` uses `VITE_API_URL`. Does not change `npm run dev` or default `npm run build`. |

**`public/data/*.json`** — Safe to commit; they are small-ish snapshots. The web app only loads them when `VITE_DATA_MODE=local` is set at build time. Regenerate after backend/journey/ladder/content changes if you ship Android offline builds.

**When to use a separate branch:** Only if you are experimenting with Capacitor config or breaking Android changes and want isolation before merge. Day-to-day, merge with the web app.

---

## Redeploy on your phone (repeatable process)

Use this whenever you (or someone) asks to **refresh offline data and install the debug APK** on a USB-connected device.

### Preconditions

- Phone: **USB debugging** on, cable connected, authorize this PC if prompted.
- PC: **JDK**, **Android SDK** / `local.properties`, **Node** dependencies in `frontend/` (`npm install` once).
- If `export:offline-data` is needed: **nothing** else should hold **port 5173** (or stop the old process first).

### Steps (copy-paste)

From the **repository root** (PowerShell on Windows):

```powershell
# 1) Free port 5173 if a stale backend is still running (optional)
.\scripts\free-port-5173.ps1 -Kill

# 2) Start backend (local profile, H2) — leave this running in a terminal, OR run in background
.\gradlew.bat bootRun --args='--spring.profiles.active=local'
```

Wait until the log shows Tomcat started on **5173** (~10–20 s).

Then from **`frontend/`**:

```bash
npm run export:offline-data
npm run android:install
```

- **`export:offline-data`** — Regenerates `public/data/journey-offline.json`, `catalog-offline.json`, `ladder-offline.json` from `http://localhost:5173/api`. **Skip** only if you did not change journey/ladder YAML/DB content and you only changed unrelated UI (then `npm run android:install` alone is enough).
- **`android:install`** — `build:android` → `cap sync` → Gradle `installDebug` on the connected device.

### If `installDebug` fails

- `No connected devices` / `adb` errors: reconnect USB, run `adb devices` (from Android SDK `platform-tools`), accept debugging on the phone, retry `npm run android:install`.
- Port 5173 busy: run `.\scripts\free-port-5173.ps1 -Kill` from repo root, then start `bootRun` again.

### One-liner (after backend is already up on 5173)

```bash
cd frontend && npm run export:offline-data && npm run android:install
```

---

## Prerequisites

- **JDK** (for Gradle) and **Node 18+** with `frontend` dependencies installed (`npm install` in `frontend/`).
- **Android SDK** — set `sdk.dir` in `frontend/android/local.properties` (see `local.properties.example` if present).
- **Device:** USB debugging enabled, or an emulator running.
- **Verify device:** `adb devices` should list your phone.

## One-time env: `frontend/.env.android`

Android builds use **`vite build --mode android`**, which loads **`frontend/.env.android`** (not committed).

1. Copy `frontend/.env.android.example` → `frontend/.env.android`.
2. Choose a mode:
   - **Offline-first (airplane mode, no PC backend):** keep  
     `VITE_DATA_MODE=local`  
     The app reads **`public/data/journey-offline.json`**, **`public/data/catalog-offline.json`**, and **`public/data/ladder-offline.json`** (home journey, subjects/exercises, ladders). Regenerate with `npm run export:offline-data` after backend/content changes.
   - **Full API on LAN:** set  
     `VITE_DATA_MODE=remote`  
     `VITE_API_URL=http://<YOUR_PC_LAN_IP>:5173/api`  
     Run the backend on the same Wi‑Fi with `--spring.profiles.active=local` (see README). The manifest allows HTTP cleartext for LAN (`usesCleartextTraffic`).

`prebuild:android` can create `.env.android` from the example if the file is missing.

## Build & install on the phone

From **`frontend/`**:

```bash
npm run android:install
```

This runs: TypeScript check → Vite **android** build → `cap sync android` → Gradle `installDebug`.

**Windows:** the script uses `gradlew.bat` under `frontend/android/`.

## Checklist before you expect it to work

| Check | Why |
|--------|-----|
| Backend on **5173** if using remote API | `application.yml` sets `server.port` to `${PORT:5173}`. |
| Same Wi‑Fi for phone + PC (remote mode) | LAN IP must match `VITE_API_URL`. |
| **CORS** when hitting PC from the device | `application-local.yml` uses `app.cors.origins: "*"` for the `local` profile. |
| **Regenerate offline JSON** after journey, ladder YAML, or DB content changes | With `VITE_DATA_MODE=local`, run backend then `npm run export:offline-data` in `frontend/` (writes journey + catalog + ladder JSON). |
| Icons present | `public/icon-192.png` and `public/icon-512.png` for install / splash branding. |

## Commands reference (frontend `package.json`)

| Script | Purpose |
|--------|---------|
| `npm run build:android` | Production web build for Android (`--mode android`). |
| `npm run android:sync` | `build:android` + `cap sync android`. |
| `npm run android:install` | `android:sync` + install debug APK on connected device. |
| `npm run export:journey-offline` | Regenerate `public/data/journey-offline.json` (API must be up, default `http://localhost:5173/api`). |
| `npm run export:catalog-offline` | Regenerate `public/data/catalog-offline.json` (subjects + exercises for plane mode). |
| `npm run export:ladder-offline` | Regenerate `public/data/ladder-offline.json` (ladder YAML + list + mixes). |
| `npm run export:offline-data` | Runs journey + catalog + ladder exports. |

## Troubleshooting

- **“Backend not available” / HTML instead of JSON** — Usually wrong or missing `VITE_API_URL` on native, or fetch hitting the static host. For remote mode, set `VITE_API_URL` to `http://<PC-LAN-IP>:5173/api` and rebuild.
- **“Cannot reach backend” on journey** — With `VITE_DATA_MODE` **not** `local`, the app calls the API; start Spring Boot on 5173 or switch to `local` for bundled journey.
- **Cleartext HTTP blocked** — Release builds may need a stricter network config; debug uses cleartext for LAN HTTP.
- **Install fails: no device** — Connect USB, authorize debugging, or start an emulator; rerun `npm run android:install`.
- **“Missing bundled catalog (404)” on Ladder / mix** — Usually a **wrong relative URL** on nested routes (fixed in `frontend/src/utils/bundledDataUrl.ts`). Rebuild and reinstall; ensure `export:offline-data` was run so `public/data/*.json` exist.

## Related files

- `frontend/src/api/config.ts` — `API_BASE`, native vs web behavior.
- `frontend/src/api/journey.ts` — bundled journey when `VITE_DATA_MODE=local`.
- `frontend/src/api/offlineCatalog.ts` / `subjects.ts` / `session.ts` — bundled catalog when `VITE_DATA_MODE=local`.
- `frontend/src/api/offlineLadderEngine.ts` / `ladder.ts` — bundled ladders when `VITE_DATA_MODE=local`.
- `frontend/src/utils/bundledDataUrl.ts` — root-relative URLs for `public/data/*.json` (avoids `/ladder/data/...` 404s).
- `GET /api/ladders/offline-bundle` — JSON used to regenerate `ladder-offline.json`.
- `frontend/capacitor.config.ts` — Capacitor app id / web dir.
- `frontend/android/app/src/main/AndroidManifest.xml` — cleartext, permissions.
