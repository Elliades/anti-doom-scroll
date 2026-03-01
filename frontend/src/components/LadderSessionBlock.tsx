import { useCallback, useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { startLadderSession, getNextLadderExercise } from '../api/ladder'
import type { ExerciseDto, LadderStateDto, LadderSessionResponseDto } from '../types/api'
import type { ExerciseResult } from '../types/exercise'
import { ExercisePlayer } from './ExercisePlayer'

function formatPercent(v: number): string {
  return `${Math.round(v * 100)}%`
}

const ANSWERS_NEEDED = 5
/**
 * How long to wait after an exercise completes before loading the next one.
 * Gives the player time to see the inline ✓ / ✗ feedback inside the exercise.
 */
const POST_EXERCISE_DELAY_MS = 1200

export interface LadderSessionBlockProps {
  ladderCode?: string
}

export function LadderSessionBlock({ ladderCode = 'default' }: LadderSessionBlockProps) {
  const [loading, setLoading] = useState(true)
  const [inlineError, setInlineError] = useState<string | null>(null)
  const [exercise, setExercise] = useState<ExerciseDto | null>(null)
  const [ladderState, setLadderState] = useState<LadderStateDto | null>(null)
  /**
   * Counter that forces ExercisePlayer to remount on every new exercise, even when
   * the backend returns the same exercise entity ID (e.g. math exercises generated
   * from the same DB row).  Without this, key={exercise.id} would remain unchanged
   * and the player would stay stuck in the answered state.
   */
  const [exerciseKey, setExerciseKey] = useState(0)
  /** Non-blocking toast shown when the player changes level. Auto-dismissed after 3 s. */
  const [levelToast, setLevelToast] = useState<{ from: number; to: number; direction: string } | null>(null)

  /**
   * Use refs for values that must be read at async-call time, not captured in closures.
   * This avoids stale-closure bugs without adding them to useCallback deps.
   */
  const ladderStateRef = useRef<LadderStateDto | null>(null)
  const transitioningRef = useRef(false)
  const toastTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  useEffect(() => { ladderStateRef.current = ladderState }, [ladderState])

  const clearToastTimer = () => {
    if (toastTimerRef.current) clearTimeout(toastTimerRef.current)
  }

  const loadInitial = useCallback(async () => {
    setLoading(true)
    setInlineError(null)
    setLevelToast(null)
    transitioningRef.current = false
    clearToastTimer()
    try {
      const data: LadderSessionResponseDto = await startLadderSession(undefined, ladderCode)
      setExercise(data.exercise)
      setLadderState(data.ladderState)
      setExerciseKey(k => k + 1)
    } catch (e) {
      setInlineError(e instanceof Error ? e.message : 'Failed to start ladder')
    } finally {
      setLoading(false)
    }
  }, [ladderCode])

  useEffect(() => {
    loadInitial()
    return clearToastTimer
  }, [loadInitial])

  /**
   * Called by ExercisePlayer when the player completes an exercise.
   *
   * Reads ladder state from a ref (never stale), waits briefly so the player sees
   * the ✓ / ✗ inline feedback, then fetches the next exercise and transitions
   * smoothly — no blocking score screen, no "Continue" button.
   */
  const handleComplete = useCallback(async (result: ExerciseResult) => {
    if (transitioningRef.current) return
    const state = ladderStateRef.current
    if (!state) return

    transitioningRef.current = true
    setInlineError(null)

    try {
      const [next] = await Promise.all([
        getNextLadderExercise(state, result.score),
        new Promise<void>(resolve => setTimeout(resolve, POST_EXERCISE_DELAY_MS)),
      ])

      setLadderState(next.ladderState)

      if (next.levelChanged) {
        clearToastTimer()
        setLevelToast(next.levelChanged)
        toastTimerRef.current = setTimeout(() => setLevelToast(null), 3000)
      }

      if (next.exercise) {
        setExercise(next.exercise)
        // Always increment the key so ExercisePlayer remounts even when the
        // backend returns the same exercise entity ID (math exercises, etc.)
        setExerciseKey(k => k + 1)
      } else {
        setInlineError('No exercise available at this level — try again.')
      }
    } catch (e) {
      setInlineError(e instanceof Error ? e.message : 'Failed to get next exercise')
    } finally {
      transitioningRef.current = false
    }
  }, []) // stable: reads state from refs, never needs re-creation

  if (loading) {
    return (
      <div className="screen center">
        <p className="pulse">Starting ladder…</p>
      </div>
    )
  }

  if (!exercise && !loading) {
    return (
      <div className="screen center">
        <p className="error">{inlineError ?? 'No exercise available.'}</p>
        <button onClick={loadInitial}>Retry</button>
        <Link to={ladderCode === 'sum' ? '/ladder' : '/'}>Back</Link>
      </div>
    )
  }

  const backUrl = ladderCode === 'sum' ? '/ladder' : '/'

  const currentScoreRaw =
    ladderState && ladderState.recentScores.length > 0
      ? ladderState.recentScores.reduce((a, b) => a + b, 0) / ladderState.recentScores.length
      : null
  const currentScore = currentScoreRaw !== null ? formatPercent(currentScoreRaw) : '—'
  const overallScoreRaw =
    ladderState && ladderState.overallTotal > 0
      ? ladderState.overallScoreSum / ladderState.overallTotal
      : null
  const overallScore = overallScoreRaw !== null ? formatPercent(overallScoreRaw) : '—'

  const scoreColor = (v: number | null) =>
    v === null ? 'var(--text-dim)' : v >= 0.75 ? 'var(--correct)' : v >= 0.4 ? '#f59e0b' : 'var(--incorrect)'

  const recentDots = Array.from({ length: ANSWERS_NEEDED }, (_, i) => {
    const s = ladderState?.recentScores[i]
    if (s === undefined) return 'empty'
    return s >= 0.5 ? 'correct' : 'incorrect'
  })

  return (
    <div className="screen">
      <header className="header ladder-header">
        <Link to={backUrl} className="back-link" style={{ marginRight: 'auto' }}>
          ← Back
        </Link>
        <span className="badge">{ladderCode === 'sum' ? 'Sum Ladder' : 'Ladder'}</span>
      </header>

      <div className="ladder-metrics" aria-label="Ladder progress">
        <div className="ladder-metric">
          <span className="ladder-metric-label">Level</span>
          <span className="ladder-metric-value ladder-metric-level">
            {ladderState?.currentLevelIndex ?? 0}
          </span>
        </div>

        <div className="ladder-metric ladder-metric--mid">
          <span className="ladder-metric-label">Current</span>
          <div className="ladder-score-dots" aria-hidden="true">
            {recentDots.map((state, i) => (
              <span key={i} className={`ladder-score-dot ladder-score-dot--${state}`} />
            ))}
          </div>
          <span className="ladder-metric-value" style={{ color: scoreColor(currentScoreRaw) }}>
            {currentScore}
          </span>
        </div>

        <div className="ladder-metric">
          <span className="ladder-metric-label">Overall</span>
          <span className="ladder-metric-value" style={{ color: scoreColor(overallScoreRaw) }}>
            {overallScore}
          </span>
        </div>
      </div>

      {levelToast && (
        <div
          className={`level-toast ${levelToast.direction === 'up' ? 'level-toast-up' : 'level-toast-down'}`}
          role="status"
          aria-live="polite"
        >
          {levelToast.direction === 'up'
            ? `↑ Level ${levelToast.to} — great work!`
            : `↓ Level ${levelToast.to} — keep going!`}
        </div>
      )}

      <main className="main">
        {inlineError && (
          <div className="inline-error" role="alert">
            <span>{inlineError}</span>
            <button type="button" onClick={loadInitial} className="retry-btn">Retry</button>
          </div>
        )}
        {exercise && (
          <ExercisePlayer
            key={`${exercise.id}-${exerciseKey}`}
            exercise={exercise}
            onComplete={(result, _elapsed) => {
              void handleComplete(typeof result === 'number' ? { score: result } : result)
            }}
          />
        )}
        {transitioningRef.current && (
          <div className="ladder-transitioning" aria-hidden="true" />
        )}
      </main>

      <footer className="footer">
        <Link to={backUrl} className="link-btn">
          End
        </Link>
      </footer>
    </div>
  )
}
