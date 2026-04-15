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
const POST_EXERCISE_DELAY_MS = 1200

export interface LadderSessionBlockProps {
  ladderCode?: string
}

export function LadderSessionBlock({ ladderCode = 'default' }: LadderSessionBlockProps) {
  const [loading, setLoading] = useState(true)
  const [inlineError, setInlineError] = useState<string | null>(null)
  const [exercise, setExercise] = useState<ExerciseDto | null>(null)
  const [ladderState, setLadderState] = useState<LadderStateDto | null>(null)
  const [levelCount, setLevelCount] = useState(0)
  const [exerciseKey, setExerciseKey] = useState(0)
  const [levelToast, setLevelToast] = useState<{ from: number; to: number; direction: string } | null>(null)
  const [isFirstExerciseInLadder, setIsFirstExerciseInLadder] = useState(true)

  const ladderStateRef = useRef<LadderStateDto | null>(null)
  const levelCountRef = useRef(0)
  const transitioningRef = useRef(false)
  const toastTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  useEffect(() => { ladderStateRef.current = ladderState }, [ladderState])
  useEffect(() => { levelCountRef.current = levelCount }, [levelCount])

  const clearToastTimer = () => {
    if (toastTimerRef.current) clearTimeout(toastTimerRef.current)
  }

  const loadInitial = useCallback(async () => {
    setLoading(true)
    setInlineError(null)
    setLevelToast(null)
    setIsFirstExerciseInLadder(true)
    transitioningRef.current = false
    clearToastTimer()
    try {
      const data: LadderSessionResponseDto = await startLadderSession(undefined, ladderCode)
      setExercise(data.exercise)
      setLadderState(data.ladderState)
      setLevelCount(data.levelCount ?? 0)
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
        setIsFirstExerciseInLadder(false)
        setExerciseKey(k => k + 1)
      } else {
        setInlineError('No exercise available at this level — try again.')
      }
    } catch (e) {
      setInlineError(e instanceof Error ? e.message : 'Failed to get next exercise')
    } finally {
      transitioningRef.current = false
    }
  }, [])

  const handleSkip = useCallback(async () => {
    if (transitioningRef.current) return
    const state = ladderStateRef.current
    if (!state) return

    transitioningRef.current = true
    setInlineError(null)

    try {
      const next = await getNextLadderExercise(state, 0.5)
      // Keep the original state — skip doesn't record a score
      if (next.exercise) {
        setExercise(next.exercise)
        setExerciseKey(k => k + 1)
      } else {
        setInlineError('No exercise available at this level — try again.')
      }
    } catch (e) {
      setInlineError(e instanceof Error ? e.message : 'Failed to skip exercise')
    } finally {
      transitioningRef.current = false
    }
  }, [])

  const handleLevelChange = useCallback(async (delta: number) => {
    if (transitioningRef.current) return
    const state = ladderStateRef.current
    if (!state) return
    const maxLevel = levelCountRef.current - 1
    const newLevel = Math.max(0, Math.min(maxLevel, state.currentLevelIndex + delta))
    if (newLevel === state.currentLevelIndex) return

    transitioningRef.current = true
    setInlineError(null)

    const from = state.currentLevelIndex
    const adjustedState: LadderStateDto = {
      ...state,
      currentLevelIndex: newLevel,
      recentScores: [],
    }
    setLadderState(adjustedState)
    ladderStateRef.current = adjustedState

    clearToastTimer()
    setLevelToast({ from, to: newLevel, direction: delta > 0 ? 'up' : 'down' })
    toastTimerRef.current = setTimeout(() => setLevelToast(null), 3000)

    try {
      const next = await getNextLadderExercise(adjustedState, 0.5)
      // Keep our adjusted state — level change doesn't record a score
      if (next.exercise) {
        setExercise(next.exercise)
        setExerciseKey(k => k + 1)
      } else {
        setInlineError('No exercise available at this level — try again.')
      }
    } catch (e) {
      setInlineError(e instanceof Error ? e.message : 'Failed to get next exercise')
    } finally {
      transitioningRef.current = false
    }
  }, [])

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
  const maxLevel = levelCount - 1
  const currentLevel = ladderState?.currentLevelIndex ?? 0

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
          <div className="ladder-level-row">
            <button
              type="button"
              className="ladder-level-btn"
              disabled={currentLevel <= 0}
              onClick={() => void handleLevelChange(-1)}
              aria-label="Level down"
            >−</button>
            <span className="ladder-metric-value ladder-metric-level">
              {currentLevel}
            </span>
            <button
              type="button"
              className="ladder-level-btn"
              disabled={currentLevel >= maxLevel}
              onClick={() => void handleLevelChange(1)}
              aria-label="Level up"
            >+</button>
          </div>
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
            showInstruction={isFirstExerciseInLadder}
            onSkip={handleSkip}
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
