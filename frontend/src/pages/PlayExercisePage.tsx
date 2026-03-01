import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { getExerciseById } from '../api/session'
import { ScoreAnimation } from '../components/ScoreAnimation'
import type { ExerciseDto } from '../types/api'
import { ExercisePlayer } from '../components/ExercisePlayer'
import { getExerciseTypeLabel } from '../utils/exerciseLabels'

/**
 * Dedicated page to play a single exercise by ID. Uses the same ExercisePlayer
 * as the journey flow so behavior is identical. Layout is minimal: back link + player.
 */
export function PlayExercisePage() {
  const { exerciseId } = useParams<{ exerciseId: string }>()
  const [exercise, setExercise] = useState<ExerciseDto | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [done, setDone] = useState(false)
  const [result, setResult] = useState<{ score: number; elapsedMs: number; subscores?: { label: string; value: string | number }[] } | null>(null)

  useEffect(() => {
    if (!exerciseId) {
      setLoading(false)
      setError('Missing exercise ID')
      return
    }
    let cancelled = false
    getExerciseById(exerciseId)
      .then((ex) => {
        if (!cancelled) {
          setExercise(ex ?? null)
          if (ex == null) setError('Exercise not found')
        }
      })
      .catch((e) => !cancelled && setError(e.message))
      .finally(() => !cancelled && setLoading(false))
    return () => { cancelled = true }
  }, [exerciseId])

  if (loading) return <div className="screen center"><p className="pulse">Loading…</p></div>
  if (error || !exercise) {
    return (
      <div className="screen center">
        <p className="error">{error ?? 'Exercise not found'}</p>
        <Link to="/subjects">Back to subjects</Link>
      </div>
    )
  }

  if (done) {
    return (
      <div className="screen center">
        {result && (
          <ScoreAnimation
            score={result.score}
            elapsedMs={result.elapsedMs}
            subscores={result.subscores}
          />
        )}
        {!result && <p className="correct">Exercise complete!</p>}
        <div className="nav-links">
          {exercise.subjectCode ? (
            <Link to={`/subjects/${exercise.subjectCode}`}>Back to subject</Link>
          ) : null}
          <Link to="/subjects">All subjects</Link>
        </div>
      </div>
    )
  }

  return (
    <div className="screen">
      <header className="header">
        <Link to={`/subjects/${exercise.subjectCode ?? ''}`} className="back-link">← Back</Link>
        <span className="badge">{exercise.subjectCode ?? 'Exercise'}</span>
        <span className="step">{getExerciseTypeLabel(exercise)} · {exercise.difficulty}</span>
      </header>
      <main className="main">
        <ExercisePlayer
          exercise={exercise}
          onComplete={(res, elapsedMs) => {
            setResult({ score: res.score, elapsedMs: elapsedMs ?? 0, subscores: res.subscores })
            setDone(true)
          }}
        />
      </main>
    </div>
  )
}
