import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { getSubjectByCode, listExercisesBySubject } from '../api/subjects'
import type { ExerciseDto } from '../types/api'
import type { SubjectDto } from '../api/subjects'
import { getExerciseTypeLabel } from '../utils/exerciseLabels'

/**
 * Subject detail: list exercises for this subject. Each exercise links to /play/:exerciseId.
 */
export function SubjectDetailPage() {
  const { code } = useParams<{ code: string }>()
  const [subject, setSubject] = useState<SubjectDto | null>(null)
  const [exercises, setExercises] = useState<ExerciseDto[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!code) {
      setLoading(false)
      setError('Missing subject code')
      return
    }
    let cancelled = false
    Promise.all([
      getSubjectByCode(code),
      listExercisesBySubject(code),
    ])
      .then(([subj, exs]) => {
        if (!cancelled) {
          setSubject(subj ?? null)
          setExercises(exs)
          if (subj == null) setError('Subject not found')
        }
      })
      .catch((e) => !cancelled && setError(e.message))
      .finally(() => !cancelled && setLoading(false))
    return () => { cancelled = true }
  }, [code])

  if (loading) return <div className="screen center"><p className="pulse">Loading…</p></div>
  if (error || !subject) {
    return (
      <div className="screen center">
        <p className="error">{error ?? 'Subject not found'}</p>
        <Link to="/subjects">Back to subjects</Link>
      </div>
    )
  }

  return (
    <div className="screen">
      <header className="header">
        <Link to="/subjects" className="back-link">← Subjects</Link>
        <h1>{subject.name}</h1>
        {subject.description && <p className="muted">{subject.description}</p>}
      </header>
      <main className="main">
        <p className="step">Pick an exercise to play:</p>
        <ul className="exercise-list">
          {exercises.map((ex) => (
            <li key={ex.id}>
              <Link to={`/play/${ex.id}`}>
                {getExerciseTypeLabel(ex)} · {ex.difficulty}
                {ex.prompt ? ` — ${ex.prompt.slice(0, 60)}${ex.prompt.length > 60 ? '…' : ''}` : ''}
              </Link>
            </li>
          ))}
        </ul>
        {exercises.length === 0 && <p className="muted">No exercises for this subject yet.</p>}
      </main>
    </div>
  )
}
