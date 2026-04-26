import type {
  LadderSessionResponseDto,
  LadderStateDto,
  LadderNextResponseDto,
  LadderMixSessionResponseDto,
  LadderMixStateDto,
  LadderMixNextResponseDto,
} from '../types/api'
import { API_BASE, ensureJsonResponse } from './config'
import { isCatalogOfflineMode } from './offlineCatalog'
import {
  offlineGetNextLadderExercise,
  offlineGetNextLadderMixExercise,
  offlineListLadderMixes,
  offlineListLadders,
  offlineStartLadderMixSession,
  offlineStartLadderSession,
} from './offlineLadderEngine'

export interface LadderSummaryDto {
  code: string
  name: string | null
  levelCount: number
}

export interface LadderMixSummaryDto {
  code: string
  name: string | null
  ladderCodes: string[]
}

export async function listLadders(): Promise<LadderSummaryDto[]> {
  if (isCatalogOfflineMode()) {
    return offlineListLadders()
  }
  const res = await fetch(`${API_BASE}/ladders`)
  ensureJsonResponse(res)
  if (!res.ok) throw new Error(`Failed to load ladders: ${res.status}`)
  return res.json()
}

export async function listLadderMixes(): Promise<LadderMixSummaryDto[]> {
  if (isCatalogOfflineMode()) {
    return offlineListLadderMixes()
  }
  const res = await fetch(`${API_BASE}/ladders/mixes`)
  if (res.status === 404) return [] // Backend may not expose mixes yet
  ensureJsonResponse(res)
  if (!res.ok) throw new Error(`Failed to load ladder mixes: ${res.status}`)
  return res.json()
}

export async function startLadderMixSession(
  profileId?: string,
  mixCode: string = 'mix'
): Promise<LadderMixSessionResponseDto> {
  if (isCatalogOfflineMode()) {
    return offlineStartLadderMixSession(mixCode)
  }
  const params = new URLSearchParams()
  params.set('mode', 'ladderMix')
  params.set('ladderMixCode', mixCode)
  if (profileId) params.set('profileId', profileId)
  const res = await fetch(`${API_BASE}/session/start?${params.toString()}`)
  ensureJsonResponse(res)
  if (!res.ok) throw new Error(`Ladder mix start failed: ${res.status}`)
  return res.json()
}

export async function getNextLadderMixExercise(
  ladderMixState: LadderMixStateDto,
  lastCompletedLadderCode: string,
  lastScore: number
): Promise<LadderMixNextResponseDto> {
  if (isCatalogOfflineMode()) {
    return offlineGetNextLadderMixExercise(ladderMixState, lastCompletedLadderCode, lastScore)
  }
  const res = await fetch(`${API_BASE}/session/ladder-mix/next`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      ladderMixState,
      lastCompletedLadderCode,
      lastScore,
    }),
  })
  ensureJsonResponse(res)
  if (!res.ok) throw new Error(`Ladder mix next failed: ${res.status}`)
  return res.json()
}

export async function startLadderSession(
  profileId?: string,
  ladderCode: string = 'default'
): Promise<LadderSessionResponseDto> {
  if (isCatalogOfflineMode()) {
    return offlineStartLadderSession(ladderCode)
  }
  const params = new URLSearchParams()
  params.set('mode', 'ladder')
  params.set('ladderCode', ladderCode)
  if (profileId) params.set('profileId', profileId)
  const res = await fetch(`${API_BASE}/session/start?${params.toString()}`)
  ensureJsonResponse(res)
  if (!res.ok) throw new Error(`Ladder start failed: ${res.status}`)
  return res.json()
}

export async function getNextLadderExercise(
  ladderState: LadderStateDto,
  lastScore: number
): Promise<LadderNextResponseDto> {
  if (isCatalogOfflineMode()) {
    return offlineGetNextLadderExercise(ladderState, lastScore)
  }
  const res = await fetch(`${API_BASE}/session/ladder/next`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      ladderState,
      lastScore,
    }),
  })
  ensureJsonResponse(res)
  if (!res.ok) throw new Error(`Ladder next failed: ${res.status}`)
  return res.json()
}
