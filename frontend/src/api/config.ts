import { Capacitor } from '@capacitor/core'

/**
 * API base URL.
 * - **Browser dev:** relative `/api` (Vite proxy → backend :5173).
 * - **Capacitor / native:** must set `VITE_API_URL` at build time (see `frontend/.env.android.example`),
 *   e.g. `http://192.168.x.x:5173/api` with backend `--spring.profiles.active=local` on the same Wi‑Fi.
 * - **Hosted PWA:** set `VITE_API_URL` to your API origin + `/api`.
 */
function resolveApiBase(): string {
  const trimmed = (import.meta.env.VITE_API_URL as string | undefined)?.trim()
  const fromEnv = trimmed?.replace(/\/$/, '')
  if (fromEnv) return fromEnv
  if (Capacitor.isNativePlatform()) {
    // Bundled JSON mode never calls the API, but config still loads because remoteProvider imports api/*.
    if (import.meta.env.VITE_DATA_MODE === 'local') return '/api'
    throw new Error(
      'Native build is missing VITE_API_URL. Copy frontend/.env.android.example to frontend/.env.android, set your API URL, then run npm run build:android && npx cap sync android.'
    )
  }
  return '/api'
}

export const API_BASE = resolveApiBase()

/**
 * Call before res.json() to avoid "Unexpected token '<'" when the server returns HTML
 * (e.g. Capacitor still using relative /api, or Firebase Hosting SPA fallback).
 */
export function ensureJsonResponse(res: Response): void {
  const ct = res.headers.get('content-type') ?? ''
  if (!ct.includes('application/json')) {
    const nativeHint = Capacitor.isNativePlatform()
      ? ' Configure VITE_API_URL in frontend/.env.android (e.g. http://YOUR_PC_LAN_IP:5173/api) and rebuild; use backend with --spring.profiles.active=local.'
      : ''
    throw new Error(
      'Backend not available. Deploy the backend and set VITE_API_URL to its URL, or run the app locally with the backend on port 5173.' +
        nativeHint
    )
  }
}
