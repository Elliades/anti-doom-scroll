import type { SessionResponseDto } from '../types/api'

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
