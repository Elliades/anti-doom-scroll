import type { ExerciseDto } from '../types/api'
import { API_BASE, ensureJsonResponse } from './config'
import { isCatalogOfflineMode, loadCatalogOfflineBundle } from './offlineCatalog'

export interface SubjectDto {
  id: string
  code: string
  name: string
  description: string | null
  parentSubjectId: string | null
  scoringConfig: {
    accuracyType: string
    speedTargetMs: number | null
    confidenceWeight: number
    streakBonusCap: number
    partialMatchThreshold: number | null
  }
}

export async function listSubjects(): Promise<SubjectDto[]> {
  if (isCatalogOfflineMode()) {
    const b = await loadCatalogOfflineBundle()
    return b.subjects
  }
  const res = await fetch(`${API_BASE}/subjects`)
  ensureJsonResponse(res)
  if (!res.ok) throw new Error(`Subjects list failed: ${res.status}`)
  return res.json()
}

export async function getSubjectByCode(code: string): Promise<SubjectDto | null> {
  if (isCatalogOfflineMode()) {
    const b = await loadCatalogOfflineBundle()
    return b.subjects.find((s) => s.code === code) ?? null
  }
  const res = await fetch(`${API_BASE}/subjects/${encodeURIComponent(code)}`)
  if (res.status === 404) return null
  ensureJsonResponse(res)
  if (!res.ok) throw new Error(`Subject fetch failed: ${res.status}`)
  return res.json()
}

export async function listExercisesBySubject(code: string): Promise<ExerciseDto[]> {
  if (isCatalogOfflineMode()) {
    const b = await loadCatalogOfflineBundle()
    return b.allExercises.filter((e) => e.subjectCode === code)
  }
  const res = await fetch(`${API_BASE}/subjects/${encodeURIComponent(code)}/exercises`)
  ensureJsonResponse(res)
  if (!res.ok) throw new Error(`Exercises list failed: ${res.status}`)
  return res.json()
}

/** List all exercises across all subjects. For testing/browsing. */
export async function listAllExercises(): Promise<ExerciseDto[]> {
  if (isCatalogOfflineMode()) {
    const b = await loadCatalogOfflineBundle()
    return b.allExercises
  }
  const res = await fetch(`${API_BASE}/exercises`)
  ensureJsonResponse(res)
  if (!res.ok) throw new Error(`Exercises list failed: ${res.status}`)
  return res.json()
}
