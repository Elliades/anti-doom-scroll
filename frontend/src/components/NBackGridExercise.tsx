import { useState, useEffect, useCallback } from 'react'
import type { ExerciseDto } from '../types/api'
import type { ExerciseResult } from '../types/exercise'

interface NBackGridExerciseProps {
  exercise: ExerciseDto
  onComplete?: (result: ExerciseResult | number) => void
  showInstruction?: boolean
}

/**
 * Grid N-Back: 3×3 grid, one cell highlighted per stimulus.
 * User taps Match when the highlighted position matches N steps back.
 */
export function NBackGridExercise({ exercise, onComplete, showInstruction = true }: NBackGridExerciseProps) {
  const params = exercise.nBackGridParams ?? exercise.nbackGridParams
  if (!params || !params.sequence?.length) {
    return <p className="error">Invalid Grid N-Back exercise: missing sequence.</p>
  }

  const [phase, setPhase] = useState<'intro' | 'playing' | 'done'>('intro')
  const [index, setIndex] = useState(0)
  const [userMatches, setUserMatches] = useState<Set<number>>(new Set())
  const [showMatchFeedback, setShowMatchFeedback] = useState<boolean | null>(null)

  const intervalMs = 1700
  const matchIndicesSet = new Set(params.matchIndices ?? [])
  const n = params.n ?? 1
  const gridSize = params.gridSize ?? 3
  const cellCount = gridSize * gridSize

  const handleMatchTap = useCallback(() => {
    if (phase !== 'playing') return
    setUserMatches((prev) => new Set(prev).add(index))
    setShowMatchFeedback(true)
    setTimeout(() => setShowMatchFeedback(null), 300)
  }, [phase, index])

  useEffect(() => {
    if (phase !== 'playing') return
    const onKey = (e: KeyboardEvent) => {
      if (e.key === ' ' || e.key === 'm' || e.key === 'M') {
        e.preventDefault()
        handleMatchTap()
      }
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [phase, handleMatchTap])

  useEffect(() => {
    if (phase !== 'playing' || index >= params.sequence.length) return
    const t = setTimeout(() => setIndex((i) => i + 1), intervalMs)
    return () => clearTimeout(t)
  }, [phase, index, params.sequence.length, intervalMs])

  useEffect(() => {
    if (phase === 'playing' && index >= params.sequence.length) setPhase('done')
  }, [phase, index, params.sequence.length])

  const score = computeScore(userMatches, matchIndicesSet)
  const hits = intersectionSize(userMatches, matchIndicesSet)
  const misses = matchIndicesSet.size - hits
  const falseAlarms = userMatches.size - hits
  const totalTargets = matchIndicesSet.size
  const accuracy = totalTargets === 0 ? 100 : Math.round((hits / totalTargets) * 100)

  useEffect(() => {
    if (phase === 'done') {
      const subscores: { label: string; value: string | number }[] = [
        { label: 'Hits', value: `${hits}/${totalTargets}` },
      ]
      if (misses > 0) subscores.push({ label: 'Misses', value: misses })
      if (falseAlarms > 0) subscores.push({ label: 'False alarms', value: falseAlarms })
      subscores.push({ label: 'Accuracy', value: `${accuracy}%` })
      onComplete?.({ score, subscores })
    }
  }, [phase, onComplete, score, hits, misses, falseAlarms, totalTargets, accuracy])
  const handleStart = () => setPhase('playing')
  const resultMessage = getResultMessage(score)

  if (phase === 'intro') {
    return (
      <div className="nback-intro">
        <div className="nback-n-badge" aria-label={`${n}-Back`}>
          {n}-Back
        </div>
        <p className="prompt">{exercise.prompt}</p>
        {showInstruction && (
          <p className="nback-instruction">
            A cell will light up in the grid. Tap Match when the highlighted position matches the one from{' '}
            {params.n} step(s) back.
          </p>
        )}
        <button onClick={handleStart} className="nback-start-btn" autoFocus>
          Start
        </button>
      </div>
    )
  }

  if (phase === 'playing') {
    const currentPos = params.sequence[index]
    return (
      <div className="nback-playing">
        <div className="nback-n-badge" aria-label={`${n}-Back`}>
          {n}-Back
        </div>
        <div className="nback-grid-container">
          <div
            className="nback-grid"
            style={{ '--grid-size': gridSize } as React.CSSProperties}
          >
            {Array.from({ length: cellCount }, (_, i) => (
              <div
                key={i}
                className={`nback-grid-cell ${i === currentPos ? 'nback-grid-cell--active' : ''}`}
              />
            ))}
          </div>
        </div>
        {showMatchFeedback === true && (
          <div className="nback-feedback nback-feedback--correct">Correct!</div>
        )}
        <button
          onClick={handleMatchTap}
          className={`nback-match-btn ${showMatchFeedback === true ? 'nback-match-tapped' : ''}`}
          disabled={showMatchFeedback !== null}
        >
          Match
        </button>
        <div className="nback-progress">
          {index + 1} / {params.sequence.length}
        </div>
      </div>
    )
  }

  return (
    <div className="nback-done">
      <p className="nback-result">{resultMessage}</p>
      <p className="nback-score">
        Score: {Math.round(score * 100)}% · Hits: {intersectionSize(userMatches, matchIndicesSet)} /{' '}
        {matchIndicesSet.size}
      </p>
    </div>
  )
}

function computeScore(userMatches: Set<number>, matchIndices: Set<number>): number {
  if (matchIndices.size === 0) return 1
  const hits = intersectionSize(userMatches, matchIndices)
  const falseAlarms = userMatches.size - hits
  const hitBonus = hits / matchIndices.size
  const penalty = Math.min(falseAlarms * 0.1, 0.3)
  return Math.max(0, Math.min(1, hitBonus - penalty))
}

function intersectionSize(a: Set<number>, b: Set<number>): number {
  let count = 0
  for (const x of a) if (b.has(x)) count++
  return count
}

function getResultMessage(score: number): string {
  if (score >= 0.9) return 'Excellent!'
  if (score >= 0.7) return 'Well done!'
  if (score >= 0.5) return 'Good effort!'
  return 'Keep practicing!'
}
