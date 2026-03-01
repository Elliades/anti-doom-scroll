import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { listAllExercises } from '../api/subjects'
import type { ExerciseDto } from '../types/api'
import { getExerciseTypeLabel } from '../utils/exerciseLabels'

/**
 * All exercises across all subjects. For testing and playing any exercise directly.
 * Each exercise links to /play/:exerciseId.
 */
export function ExercisesListPage() {
  const [exercises, setExercises] = useState<ExerciseDto[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    listAllExercises()
      .then((data) => !cancelled && setExercises(data))
      .catch((e) => !cancelled && setError(e.message))
      .finally(() => !cancelled && setLoading(false))
    return () => { cancelled = true }
  }, [])

  if (loading) return <div className="screen center"><p className="pulse">Loading…</p></div>
  if (error) {
    return (
      <div className="screen center">
        <p className="error">{error}</p>
        <p className="muted">Ensure the backend runs with local profile: <code>.\scripts\start-backend-local.ps1</code></p>
        <Link to="/">Back to app</Link>
      </div>
    )
  }

  return (
    <div className="screen">
      <header className="header">
        <Link to="/" className="back-link">← App</Link>
        <h1>All Exercises</h1>
        <p className="muted">Pick any exercise to test/play</p>
      </header>
      <main className="main">
        <ul className="exercise-list">
          {exercises.map((ex) => (
            <li key={ex.id}>
              <Link to={`/play/${ex.id}`}>
                <span className="badge">{ex.subjectCode ?? '—'}</span>{' '}
                {getExerciseTypeLabel(ex)} · {ex.difficulty}
                {ex.prompt ? ` — ${ex.prompt.slice(0, 50)}${ex.prompt.length > 50 ? '…' : ''}` : ''}
              </Link>
            </li>
          ))}
        </ul>
        {exercises.length === 0 && (
          <p className="muted">
            No exercises. Start backend with local profile:{' '}
            <code>.\scripts\start-backend-local.ps1</code> or{' '}
            <code>.\gradlew.bat bootRun --args='--spring.profiles.active=local'</code>
          </p>
        )}
        <nav className="nav-links">
          <Link to="/subjects">Browse by subject</Link>
        </nav>
      </main>
    </div>
  )
}

