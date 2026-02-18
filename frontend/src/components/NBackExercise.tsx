import { useState, useEffect, useCallback } from 'react'
import type { ExerciseDto } from '../types/api'

interface NBackExerciseProps {
  exercise: ExerciseDto
  onComplete?: (score: number) => void
}

/**
 * 1-Back (and N-Back) working memory exercise.
 * Displays sequence one item at a time; user taps "Match" when current == item N steps back.
 * Ultra-easy: ~2.5s per letter, 1-back.
 */
export function NBackExercise({ exercise, onComplete }: NBackExerciseProps) {
  const params = exercise.nBackParams
  if (!params) {
    return <p className="error">Invalid N-Back exercise: missing sequence.</p>
  }

  const [phase, setPhase] = useState<'intro' | 'playing' | 'done'>('intro')
  const [index, setIndex] = useState(0)
  const [userMatches, setUserMatches] = useState<Set<number>>(new Set())
  const [showMatchFeedback, setShowMatchFeedback] = useState<boolean | null>(null)

  const intervalMs = 2500 // ultra-easy: slow speed
  const matchIndicesSet = new Set(params.matchIndices)

  const handleMatchTap = useCallback(() => {
    if (phase !== 'playing') return
    setUserMatches((prev) => new Set(prev).add(index))
    setShowMatchFeedback(true)
    setTimeout(() => setShowMatchFeedback(null), 300)
  }, [phase, index])

  useEffect(() => {
    if (phase !== 'playing' || index >= params.sequence.length) return
    const t = setTimeout(() => setIndex((i) => i + 1), intervalMs)
    return () => clearTimeout(t)
  }, [phase, index, params.sequence.length, intervalMs])

  useEffect(() => {
    if (phase === 'playing' && index >= params.sequence.length) {
      setPhase('done')
    }
  }, [phase, index, params.sequence.length])

  useEffect(() => {
    if (phase === 'done') onComplete?.(1)
  }, [phase, onComplete])

  const score = computeScore(userMatches, matchIndicesSet)

  const handleStart = () => setPhase('playing')
  const resultMessage = getResultMessage(score)

  if (phase === 'intro') {
    return (
      <div className="nback-intro">
        <p className="prompt">{exercise.prompt}</p>
        <p className="nback-instruction">
          Letters will appear one by one. Tap <strong>Match</strong> when the current letter
          matches the previous one ({params.n}-back).
        </p>
        <button onClick={handleStart} className="nback-start-btn">
          Start
        </button>
      </div>
    )
  }

  if (phase === 'playing') {
    const currentLetter = params.sequence[index]
    return (
      <div className="nback-playing">
        <div className="nback-letter" key={index}>
          {currentLetter}
        </div>
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

  // phase === 'done'
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
