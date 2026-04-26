import type { JourneyDto, JourneyStepContentDto } from '../types/api'
import { bundledDataUrl } from '../utils/bundledDataUrl'
import { API_BASE, ensureJsonResponse } from './config'

/** Bundled snapshot (see `public/data/journey-offline.json`) — used when `VITE_DATA_MODE=local` (Android standalone). */
interface JourneyOfflineBundle {
  journey: JourneyDto
  stepContents: Record<string, JourneyStepContentDto>
}

const useBundledJourney = import.meta.env.VITE_DATA_MODE === 'local'

let offlineCache: JourneyOfflineBundle | null = null

async function loadOfflineBundle(): Promise<JourneyOfflineBundle> {
  if (offlineCache) return offlineCache
  const url = bundledDataUrl('journey-offline.json')
  const res = await fetch(url)
  if (!res.ok) {
    throw new Error(
      `Missing bundled journey (${res.status}). Ensure public/data/journey-offline.json exists and rebuild.`
    )
  }
  offlineCache = (await res.json()) as JourneyOfflineBundle
  return offlineCache
}

function offlineStepKey(stepIndex: number, chapterIndex?: number): string {
  if (stepIndex === 2 && chapterIndex != null) return `2-${chapterIndex}`
  return String(stepIndex)
}

function journeyErrorMessage(status: number): string {
  if (status === 404) return 'Journey not found. Check backend config (app.journey in application.yml).'
  if (status >= 500) return `Server error (${status}). Check backend logs.`
  if (status === 502 || status === 503) return `Backend may not be running (${status}). Start the server on port 5173.`
  return `Journey failed: ${status}`
}

export async function getJourney(code: string = 'default'): Promise<JourneyDto> {
  if (useBundledJourney) {
    const bundle = await loadOfflineBundle()
    if (bundle.journey.code !== code) {
      throw new Error(`Journey "${code}" is not available offline (bundled: "${bundle.journey.code}").`)
    }
    return bundle.journey
  }

  try {
    const res = await fetch(`${API_BASE}/journey?code=${encodeURIComponent(code)}`)
    ensureJsonResponse(res)
    if (!res.ok) throw new Error(journeyErrorMessage(res.status))
    return res.json()
  } catch (e) {
    if (e instanceof TypeError && e.message.includes('fetch')) {
      throw new Error(
        'Cannot reach backend. Is the server running on port 5173? (Spring Boot uses server.port 5173 by default; run ./gradlew bootRun --args=\'--spring.profiles.active=local\'.)'
      )
    }
    throw e
  }
}

export async function getJourneyStepContent(
  stepIndex: number,
  options?: { journeyCode?: string; profileId?: string; chapterIndex?: number }
): Promise<JourneyStepContentDto> {
  if (useBundledJourney) {
    const bundle = await loadOfflineBundle()
    const key = offlineStepKey(stepIndex, options?.chapterIndex)
    const content = bundle.stepContents[key]
    if (!content) {
      throw new Error(`No bundled content for journey step key "${key}". Regenerate public/data/journey-offline.json.`)
    }
    return content
  }

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
