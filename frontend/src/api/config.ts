/**
 * API base URL. In production (Firebase Hosting), set VITE_API_URL to your backend URL (e.g. https://your-backend.run.app/api).
 * When unset, the app uses relative /api (works with dev proxy or same-origin backend).
 */
export const API_BASE =
  (import.meta.env.VITE_API_URL as string | undefined)?.replace(/\/$/, '') ?? '/api'

/**
 * Call before res.json() to avoid "Unexpected token '<'" when the server returns HTML
 * (e.g. Firebase Hosting SPA fallback because /api is not proxied to a backend).
 */
export function ensureJsonResponse(res: Response): void {
  const ct = res.headers.get('content-type') ?? ''
  if (!ct.includes('application/json')) {
    throw new Error(
      'Backend not available. Deploy the backend and set VITE_API_URL to its URL, or run the app locally with the backend on port 5173.'
    )
  }
}
