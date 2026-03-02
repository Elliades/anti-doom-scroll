import type { JourneyDto, JourneyStepContentDto } from '../types/api'
import { API_BASE, ensureJsonResponse } from './config'

function journeyErrorMessage(status: number): string {
  if (status === 404) return 'Journey not found. Check backend config (app.journey in application.yml).'
  if (status >= 500) return `Server error (${status}). Check backend logs.`
  if (status === 502 || status === 503) return `Backend may not be running (${status}). Start the server on port 5173.`
  return `Journey failed: ${status}`
}

export async function getJourney(code: string = 'default'): Promise<JourneyDto> {
  try {
    const res = await fetch(`${API_BASE}/journey?code=${encodeURIComponent(code)}`)
    ensureJsonResponse(res)
    if (!res.ok) throw new Error(journeyErrorMessage(res.status))
    return res.json()
  } catch (e) {
    if (e instanceof TypeError && e.message.includes('fetch')) {
      throw new Error('Cannot reach backend. Is the server running on port 5173?')
    }
    throw e
  }
}

export async function getJourneyStepContent(
  stepIndex: number,
  options?: { journeyCode?: string; profileId?: string; chapterIndex?: number }
): Promise<JourneyStepContentDto> {
  const params = new URLSearchParams()
  params.set('journeyCode', options?.journeyCode ?? 'default')
  if (options?.profileId) params.set('profileId', options.profileId)
  if (options?.chapterIndex != null) params.set('chapterIndex', String(options.chapterIndex))
  const res = await fetch(
    `${API_BASE}/journey/steps/${stepIndex}/content?${params.toString()}`
  )
  ensureJsonResponse(res)
  if (!res.ok) throw new Error(`Step content failed: ${res.status}`)
  return res.json()
}
