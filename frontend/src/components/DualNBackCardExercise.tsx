import { useState, useEffect, useCallback } from 'react'
import type { ExerciseDto } from '../types/api'
import type { ExerciseResult } from '../types/exercise'
import { NBackCardDisplay } from './NBackCardDisplay'

interface DualNBackCardExerciseProps {
  exercise: ExerciseDto
  onComplete?: (result: ExerciseResult | number) => void
  showInstruction?: boolean
}

/**
 * Dual Card N-Back: Cards shown one-by-one. User taps "Match Color" (suit) or "Match Number" (rank)
 * when that attribute matches N steps back.
 */
export function DualNBackCardExercise({ exercise, onComplete, showInstruction = true }: DualNBackCardExerciseProps) {
  const params = exercise.dualNBackCardParams ?? exercise.dualNbackCardParams
  if (!params || !params.sequence?.length) {
    return <p className="error">Invalid Dual Card N-Back exercise: missing sequence.</p>
  }

  const [phase, setPhase] = useState<'intro' | 'playing' | 'done'>('intro')
  const [index, setIndex] = useState(0)
  const [userColorMatches, setUserColorMatches] = useState<Set<number>>(new Set())
  const [userNumberMatches, setUserNumberMatches] = useState<Set<number>>(new Set())
  const [showFeedback, setShowFeedback] = useState<'color' | 'number' | 'wrong' | null>(null)

  const intervalMs = 2500
  const n = params.n ?? 1
  const matchColorSet = new Set(params.matchColorIndices ?? [])
  const matchNumberSet = new Set(params.matchNumberIndices ?? [])

  const handleColorMatch = useCallback(() => {
    if (phase !== 'playing') return
    setUserColorMatches((prev) => new Set(prev).add(index))
    setShowFeedback(matchColorSet.has(index) ? 'color' : 'wrong')
    setTimeout(() => setShowFeedback(null), 400)
  }, [phase, index, matchColorSet])

  const handleNumberMatch = useCallback(() => {
    if (phase !== 'playing') return
    setUserNumberMatches((prev) => new Set(prev).add(index))
    setShowFeedback(matchNumberSet.has(index) ? 'number' : 'wrong')
    setTimeout(() => setShowFeedback(null), 400)
  }, [phase, index, matchNumberSet])

  useEffect(() => {
    if (phase !== 'playing' || index >= params.sequence.length) return
    const t = setTimeout(() => setIndex((i) => i + 1), intervalMs)
    return () => clearTimeout(t)
  }, [phase, index, params.sequence.length, intervalMs])

  useEffect(() => {
    if (phase === 'playing' && index >= params.sequence.length) setPhase('done')
  }, [phase, index, params.sequence.length])

  const colorHits = intersectionSize(userColorMatches, matchColorSet)
  const colorMisses = matchColorSet.size - colorHits
  const colorFalseAlarms = userColorMatches.size - colorHits
  const numberHits = intersectionSize(userNumberMatches, matchNumberSet)
  const numberMisses = matchNumberSet.size - numberHits
  const numberFalseAlarms = userNumberMatches.size - numberHits
  const totalTargets = matchColorSet.size + matchNumberSet.size
  const totalHits = colorHits + numberHits
  const score = computeDualScore(userColorMatches, userNumberMatches, matchColorSet, matchNumberSet)
  const colorAccuracy = matchColorSet.size === 0 ? 100 : Math.round((colorHits / matchColorSet.size) * 100)
  const numberAccuracy = matchNumberSet.size === 0 ? 100 : Math.round((numberHits / matchNumberSet.size) * 100)
  const overallAccuracy = totalTargets === 0 ? 100 : Math.round((totalHits / totalTargets) * 100)

  useEffect(() => {
    if (phase === 'done') {
      const subscores: { label: string; value: string | number }[] = [
        { label: 'Color hits', value: `${colorHits}/${matchColorSet.size}` },
      ]
      if (colorMisses > 0) subscores.push({ label: 'Color misses', value: colorMisses })
      if (colorFalseAlarms > 0) subscores.push({ label: 'Color false alarms', value: colorFalseAlarms })
      subscores.push({ label: 'Color accuracy', value: `${colorAccuracy}%` })
      subscores.push({ label: 'Number hits', value: `${numberHits}/${matchNumberSet.size}` })
      if (numberMisses > 0) subscores.push({ label: 'Number misses', value: numberMisses })
      if (numberFalseAlarms > 0) subscores.push({ label: 'Number false alarms', value: numberFalseAlarms })
      subscores.push({ label: 'Number accuracy', value: `${numberAccuracy}%` })
      subscores.push({ label: 'Overall accuracy', value: `${overallAccuracy}%` })
      onComplete?.({ score, subscores })
    }
  }, [phase, onComplete, score, colorHits, colorMisses, colorFalseAlarms, numberHits, numberMisses, numberFalseAlarms, matchColorSet.size, matchNumberSet.size, colorAccuracy, numberAccuracy, overallAccuracy])
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
            Cards will appear one by one. Tap &quot;Match Color&quot; when the suit matches, or
            &quot;Match Number&quot; when the rank matches, {params.n} step(s) back.
          </p>
        )}
        <button onClick={handleStart} className="nback-start-btn">
          Start
        </button>
      </div>
    )
  }

  if (phase === 'playing') {
    const currentCard = params.sequence[index]
    return (
      <div className="nback-playing">
        <div className="nback-n-badge" aria-label={`${n}-Back`}>
          {n}-Back
        </div>
        <div className="nback-stimulus" key={index}>
          <NBackCardDisplay code={currentCard} size="large" />
        </div>
        {showFeedback === 'color' && (
          <div className="nback-feedback nback-feedback--correct">Correct! (Color)</div>
        )}
        {showFeedback === 'number' && (
          <div className="nback-feedback nback-feedback--correct">Correct! (Number)</div>
        )}
        {showFeedback === 'wrong' && (
          <div className="nback-feedback nback-feedback--wrong">Incorrect</div>
        )}
        <div className="nback-dual-buttons">
          <button
            onClick={handleColorMatch}
            className="nback-match-btn nback-match-btn--color"
            disabled={showFeedback !== null}
          >
            Match Color
          </button>
          <button
            onClick={handleNumberMatch}
            className="nback-match-btn nback-match-btn--number"
            disabled={showFeedback !== null}
          >
            Match Number
          </button>
        </div>
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
        Score: {Math.round(score * 100)}% · Hits: {totalHits} / {totalTargets} (Color: {colorHits}/
        {matchColorSet.size}, Number: {numberHits}/{matchNumberSet.size})
      </p>
    </div>
  )
}

function computeDualScore(
  userCol: Set<number>,
  userNum: Set<number>,
  matchCol: Set<number>,
  matchNum: Set<number>
): number {
  const totalTargets = matchCol.size + matchNum.size
  if (totalTargets === 0) return 1
  const hits = intersectionSize(userCol, matchCol) + intersectionSize(userNum, matchNum)
  const falseAlarms =
    (userCol.size - intersectionSize(userCol, matchCol)) +
    (userNum.size - intersectionSize(userNum, matchNum))
  const hitBonus = hits / totalTargets
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
