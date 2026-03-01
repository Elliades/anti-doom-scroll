import type { ExerciseDto, SessionResponseDto } from '../types/api'

const API_BASE = '/api'

export async function startSession(
  profileId?: string,
  preferType?: string,
  mode?: 'openapp'
): Promise<SessionResponseDto> {
  const params = new URLSearchParams()
  if (profileId) params.set('profileId', profileId)
  if (preferType) params.set('preferType', preferType)
  if (mode) params.set('mode', mode)
  const query = params.toString()
  const url = query ? `${API_BASE}/session/start?${query}` : `${API_BASE}/session/start`
  const res = await fetch(url)
  if (!res.ok) throw new Error(`Session start failed: ${res.status}`)
  return res.json()
}

/** Fetch N-back exercise by level (1, 2, or 3). Requires backend running; use with dev server so /api is proxied to :5173. */
export async function getNBackByLevel(level: number): Promise<ExerciseDto | null> {
  const res = await fetch(`${API_BASE}/nback/${level}`)
  if (res.status === 404) return null
  if (!res.ok) throw new Error(`N-back fetch failed: ${res.status}`)
  return res.json()
}

/** Known N-back exercise IDs -> level (1, 2, or 3). Used when exercises/{id} returns N_BACK without nBackParams. */
const NBACK_ID_TO_LEVEL: Record<string, number> = {
  'c0000000-0000-0000-0000-000000000001': 1,
  'c0000000-0000-0000-0000-000000000002': 2,
  'c0000000-0000-0000-0000-000000000003': 3,
}

/** Fetch a single exercise by ID for dedicated play page. */
export async function getExerciseById(id: string): Promise<ExerciseDto | null> {
  const res = await fetch(`${API_BASE}/exercises/${encodeURIComponent(id)}?_=${Date.now()}`)
  if (res.status === 404) return null
  if (!res.ok) throw new Error(`Exercise fetch failed: ${res.status}`)
  const ex = (await res.json()) as ExerciseDto
  const nbackParams = ex.nBackParams ?? ex.nbackParams
  // Fallback: N_BACK without nBackParams — refetch from level endpoint
  if (ex.type === 'N_BACK' && (!nbackParams || !nbackParams.sequence?.length)) {
    const level = NBACK_ID_TO_LEVEL[id]
    if (level) {
      const nback = await getNBackByLevel(level)
      const p = nback?.nBackParams ?? nback?.nbackParams
      if (p) return { ...ex, nBackParams: p, nbackParams: p }
    }
  }
  return ex
}
