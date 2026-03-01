import type { ExerciseDto } from '../types/api'
import { API_BASE, ensureJsonResponse } from './config'

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
  const res = await fetch(`${API_BASE}/subjects`)
  ensureJsonResponse(res)
  if (!res.ok) throw new Error(`Subjects list failed: ${res.status}`)
  return res.json()
}

export async function getSubjectByCode(code: string): Promise<SubjectDto | null> {
  const res = await fetch(`${API_BASE}/subjects/${encodeURIComponent(code)}`)
  if (res.status === 404) return null
  ensureJsonResponse(res)
  if (!res.ok) throw new Error(`Subject fetch failed: ${res.status}`)
  return res.json()
}

export async function listExercisesBySubject(code: string): Promise<ExerciseDto[]> {
  const res = await fetch(`${API_BASE}/subjects/${encodeURIComponent(code)}/exercises`)
  ensureJsonResponse(res)
  if (!res.ok) throw new Error(`Exercises list failed: ${res.status}`)
  return res.json()
}

/** List all exercises across all subjects. For testing/browsing. */
export async function listAllExercises(): Promise<ExerciseDto[]> {
  const res = await fetch(`${API_BASE}/exercises`)
  ensureJsonResponse(res)
  if (!res.ok) throw new Error(`Exercises list failed: ${res.status}`)
  return res.json()
}
