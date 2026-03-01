import type { LadderSessionResponseDto, LadderStateDto, LadderNextResponseDto } from '../types/api'

const API_BASE = '/api'

export async function startLadderSession(
  profileId?: string,
  ladderCode: string = 'default'
): Promise<LadderSessionResponseDto> {
  const params = new URLSearchParams()
  params.set('mode', 'ladder')
  params.set('ladderCode', ladderCode)
  if (profileId) params.set('profileId', profileId)
  const res = await fetch(`${API_BASE}/session/start?${params.toString()}`)
  if (!res.ok) throw new Error(`Ladder start failed: ${res.status}`)
  return res.json()
}

export async function getNextLadderExercise(
  ladderState: LadderStateDto,
  lastScore: number
): Promise<LadderNextResponseDto> {
  const res = await fetch(`${API_BASE}/session/ladder/next`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      ladderState,
      lastScore,
    }),
  })
  if (!res.ok) throw new Error(`Ladder next failed: ${res.status}`)
  return res.json()
}
