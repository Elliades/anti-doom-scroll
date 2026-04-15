import { useCallback, useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  startLadderMixSession,
  getNextLadderMixExercise,
} from '../api/ladder'
import type {
  ExerciseDto,
  LadderMixStateDto,
  LadderMixSessionResponseDto,
} from '../types/api'
import type { ExerciseResult } from '../types/exercise'
import { ExercisePlayer } from './ExercisePlayer'

function formatPercent(v: number): string {
  return `${Math.round(v * 100)}%`
}

const ANSWERS_NEEDED = 5
const POST_EXERCISE_DELAY_MS = 1200

const LADDER_LABELS: Record<string, string> = {
  sum: 'Sum',
  anagram: 'Anagram',
  pair: 'Pair',
  estimation: 'Estimation',
  default: 'Default',
}

export interface LadderMixSessionBlockProps {
  mixCode?: string
}

export function LadderMixSessionBlock({ mixCode = 'mix' }: LadderMixSessionBlockProps) {
  const [loading, setLoading] = useState(true)
  const [inlineError, setInlineError] = useState<string | null>(null)
  const [exercise, setExercise] = useState<ExerciseDto | null>(null)
  const [ladderMixState, setLadderMixState] = useState<LadderMixStateDto | null>(null)
  const [levelCount, setLevelCount] = useState(0)
  const [exerciseKey, setExerciseKey] = useState(0)
  const [levelToast, setLevelToast] = useState<{ from: number; to: number; direction: string } | null>(null)
  const [isFirstExerciseInLadder, setIsFirstExerciseInLadder] = useState(true)

  const ladderMixStateRef = useRef<LadderMixStateDto | null>(null)
  const levelCountRef = useRef(0)
  const currentLadderCodeRef = useRef<string | null>(null)
  const transitioningRef = useRef(false)
  const toastTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  useEffect(() => { ladderMixStateRef.current = ladderMixState }, [ladderMixState])
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
      const data: LadderMixSessionResponseDto = await startLadderMixSession(undefined, mixCode)
      setExercise(data.exercise)
      setLadderMixState(data.ladderMixState)
      setLevelCount(data.levelCount ?? 0)
      currentLadderCodeRef.current = data.ladderMixState.ladderCodes[data.ladderMixState.nextLadderIndex]
      setExerciseKey(k => k + 1)
    } catch (e) {
      setInlineError(e instanceof Error ? e.message : 'Failed to start ladder mix')
    } finally {
      setLoading(false)
    }
  }, [mixCode])

  useEffect(() => {
    loadInitial()
    return clearToastTimer
  }, [loadInitial])

  const handleComplete = useCallback(async (result: ExerciseResult) => {
    if (transitioningRef.current) return
    const state = ladderMixStateRef.current
    const lastCompletedLadderCode = currentLadderCodeRef.current
    if (!state || !lastCompletedLadderCode) return

    transitioningRef.current = true
    setInlineError(null)

    try {
      const [next] = await Promise.all([
        getNextLadderMixExercise(state, lastCompletedLadderCode, result.score),
        new Promise<void>(resolve => setTimeout(resolve, POST_EXERCISE_DELAY_MS)),
      ])

      setLadderMixState(next.ladderMixState)
      currentLadderCodeRef.current = next.ladderMixState.ladderCodes[next.ladderMixState.nextLadderIndex]

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
    const state = ladderMixStateRef.current
    const lastCompletedLadderCode = currentLadderCodeRef.current
    if (!state || !lastCompletedLadderCode) return

    transitioningRef.current = true
    setInlineError(null)

    try {
      const next = await getNextLadderMixExercise(state, lastCompletedLadderCode, 0.5)
      // Keep the original state — skip doesn't record a score
      currentLadderCodeRef.current = next.ladderMixState.ladderCodes[next.ladderMixState.nextLadderIndex]
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
    const state = ladderMixStateRef.current
    const lastCompletedLadderCode = currentLadderCodeRef.current
    if (!state || !lastCompletedLadderCode) return
    const maxLevel = levelCountRef.current - 1
    const newLevel = Math.max(0, Math.min(maxLevel, state.currentLevelIndex + delta))
    if (newLevel === state.currentLevelIndex) return

    transitioningRef.current = true
    setInlineError(null)

    const from = state.currentLevelIndex
    const adjustedState: LadderMixStateDto = {
      ...state,
      currentLevelIndex: newLevel,
      perLadderStates: Object.fromEntries(
        Object.entries(state.perLadderStates).map(([code, s]) => [
          code,
          { ...s, recentScores: [] },
        ])
      ),
    }
    setLadderMixState(adjustedState)
    ladderMixStateRef.current = adjustedState

    clearToastTimer()
    setLevelToast({ from, to: newLevel, direction: delta > 0 ? 'up' : 'down' })
    toastTimerRef.current = setTimeout(() => setLevelToast(null), 3000)

    try {
      const next = await getNextLadderMixExercise(adjustedState, lastCompletedLadderCode, 0.5)
      // Keep our adjusted state — level change doesn't record a score
      currentLadderCodeRef.current = next.ladderMixState.ladderCodes[next.ladderMixState.nextLadderIndex]
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
        <p className="pulse">Starting ladder mix…</p>
      </div>
    )
  }

  if (!exercise && !loading) {
    return (
      <div className="screen center">
        <p className="error">{inlineError ?? 'No exercise available.'}</p>
        <button onClick={loadInitial}>Retry</button>
        <Link to="/ladder">Back</Link>
      </div>
    )
  }

  const maxLevel = levelCount - 1
  const currentLevel = ladderMixState?.currentLevelIndex ?? 0

  const overallScoreSum = ladderMixState
    ? Object.values(ladderMixState.perLadderStates).reduce((a, s) => a + s.overallScoreSum, 0)
    : 0
  const overallTotal = ladderMixState
    ? Object.values(ladderMixState.perLadderStates).reduce((a, s) => a + s.overallTotal, 0)
    : 0
  const overallScoreRaw = overallTotal > 0 ? overallScoreSum / overallTotal : null
  const overallScore = overallScoreRaw !== null ? formatPercent(overallScoreRaw) : '—'

  const scoreColor = (v: number | null) =>
    v === null ? 'var(--text-dim)' : v >= 0.75 ? 'var(--correct)' : v >= 0.4 ? '#f59e0b' : 'var(--incorrect)'

  return (
    <div className="screen">
      <header className="header ladder-header">
        <Link to="/ladder" className="back-link" style={{ marginRight: 'auto' }}>
          ← Back
        </Link>
        <span className="badge">Ladder Mix</span>
      </header>

      <div className="ladder-metrics" aria-label="Ladder mix progress">
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
          <span className="ladder-metric-label">Per ladder</span>
          <div className="ladder-mix-per-ladder">
            {ladderMixState?.ladderCodes.map((code) => {
              const s = ladderMixState.perLadderStates[code]
              const avg = s?.recentScores?.length
                ? s.recentScores.reduce((a, b) => a + b, 0) / s.recentScores.length
                : null
              const recentDots = Array.from({ length: ANSWERS_NEEDED }, (_, i) => {
                const score = s?.recentScores[i]
                if (score === undefined) return 'empty'
                return score >= 0.5 ? 'correct' : 'incorrect'
              })
              return (
                <div key={code} className="ladder-mix-ladder-row">
                  <span className="ladder-mix-ladder-label">{LADDER_LABELS[code] ?? code}:</span>
                  <div className="ladder-score-dots" aria-hidden="true">
                    {recentDots.map((dotState, i) => (
                      <span key={i} className={`ladder-score-dot ladder-score-dot--${dotState}`} />
                    ))}
                  </div>
                  <span className="ladder-metric-value" style={{ color: scoreColor(avg) }}>
                    {avg !== null ? formatPercent(avg) : '—'}
                  </span>
                </div>
              )
            })}
          </div>
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
            ? `↑ Level ${levelToast.to} — all ladders passed!`
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
        <Link to="/ladder" className="link-btn">
          End
        </Link>
      </footer>
    </div>
  )
}
