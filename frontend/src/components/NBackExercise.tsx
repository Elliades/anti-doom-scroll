import { useState, useEffect, useCallback, useMemo, useRef } from 'react'
import type { ExerciseDto } from '../types/api'
import type { ExerciseResult } from '../types/exercise'
import { isCardCode } from './NBackCardDisplay'
import { NBackCardCarousel } from './NBackCardCarousel'

interface NBackExerciseProps {
  exercise: ExerciseDto
  onComplete?: (result: ExerciseResult | number) => void
  /** When false, intro screen omits the instruction paragraph (e.g. ladder: only first exercise). Default true. */
  showInstruction?: boolean
}

/**
 * N-Back working memory exercise.
 * Displays sequence one item at a time (cards or letters); user taps "Match" when current == item N steps back.
 * Uses playing cards when sequence contains card codes (e.g. AC, 2D, QH).
 */
export function NBackExercise({ exercise, onComplete, showInstruction = true }: NBackExerciseProps) {
  const params = exercise.nBackParams ?? exercise.nbackParams
  if (!params || !params.sequence?.length) {
    return <p className="error">Invalid N-Back exercise: missing sequence.</p>
  }

  const n = params.n ?? 1
  const [phase, setPhase] = useState<'intro' | 'playing' | 'done'>('intro')
  const [index, setIndex] = useState(0)
  const [userMatches, setUserMatches] = useState<Set<number>>(new Set())
  const [showMatchFeedback, setShowMatchFeedback] = useState<'correct' | 'wrong' | null>(null)
  const [showCanMatchNotice, setShowCanMatchNotice] = useState(false)
  const matchFeedbackLockRef = useRef(false)

  const intervalMs = 2500 // ultra-easy: slow speed
  const matchIndicesSet = new Set(params.matchIndices ?? [])
  const matchDisabled = index < n

  const useCards = useMemo(
    () => params.sequence.length > 0 && isCardCode(params.sequence[0]),
    [params.sequence]
  )

  const FEEDBACK_MS = 650

  const handleMatchTap = useCallback(() => {
    if (phase !== 'playing' || matchDisabled) return
    if (matchFeedbackLockRef.current) return
    matchFeedbackLockRef.current = true
    const isCorrect = matchIndicesSet.has(index)
    setUserMatches((prev) => new Set(prev).add(index))
    setShowMatchFeedback(isCorrect ? 'correct' : 'wrong')
    setTimeout(() => {
      setShowMatchFeedback(null)
      matchFeedbackLockRef.current = false
    }, FEEDBACK_MS)
  }, [phase, index, matchDisabled, matchIndicesSet])

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
    if (phase !== 'playing' || useCards || index >= params.sequence.length) return
    const t = setTimeout(() => setIndex((i) => i + 1), intervalMs)
    return () => clearTimeout(t)
  }, [phase, index, params.sequence.length, intervalMs, useCards])

  useEffect(() => {
    if (phase === 'playing' && index >= params.sequence.length) {
      setPhase('done')
    }
  }, [phase, index, params.sequence.length])

  // When first card with a match target appears (index === n), show "Match is now active!"
  useEffect(() => {
    if (phase !== 'playing') return
    if (index > n) {
      setShowCanMatchNotice(false)
      return
    }
    if (index !== n) return
    setShowCanMatchNotice(true)
    const t = setTimeout(() => setShowCanMatchNotice(false), 2500)
    return () => clearTimeout(t)
  }, [phase, index, n])

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
  const handleComplete = useCallback(() => setPhase('done'), [])
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
            {useCards
              ? 'Cards will appear one by one. Tap Match when the current card matches the one from ' + params.n + ' step(s) back.'
              : 'Items will appear one by one. Tap Match when the current item matches the one from ' + params.n + ' step(s) back.'}
          </p>
        )}
        <button onClick={handleStart} className="nback-start-btn" autoFocus>
          Start
        </button>
      </div>
    )
  }

  if (phase === 'playing') {
    const currentItem = params.sequence[index]
    const exampleM = n + 1
    return (
      <div className="nback-playing">
        <div className="nback-n-badge" aria-label={`${n}-Back`}>
          {n}-Back
        </div>
        <div
          className={[
            'nback-stimulus',
            !useCards && showMatchFeedback
              ? `nback-stimulus--feedback nback-stimulus--feedback-${showMatchFeedback}`
              : '',
          ]
            .filter(Boolean)
            .join(' ')}
        >
          {useCards ? (
            <NBackCardCarousel
              n={params.n}
              sequence={params.sequence}
              onCardShow={setIndex}
              onComplete={handleComplete}
              matchFeedback={showMatchFeedback}
            />
          ) : (
            <span className="nback-letter">{currentItem}</span>
          )}
        </div>
        {matchDisabled && (
          <p className="nback-wait-hint">No match possible yet (first {n} cards)</p>
        )}
        {showCanMatchNotice && (
          <p className="nback-can-match-notice" role="status">
            Match is now active!
          </p>
        )}
        <button
          onClick={handleMatchTap}
          className="nback-match-btn"
          disabled={matchDisabled}
        >
          Match
        </button>
        <p className="nback-example">
          Card m matches card m−{n} (e.g. card {exampleM} = card 1)
        </p>
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
