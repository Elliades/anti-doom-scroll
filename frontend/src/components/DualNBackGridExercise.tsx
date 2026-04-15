import { useState, useEffect, useCallback } from 'react'
import type { ExerciseDto } from '../types/api'
import type { ExerciseResult } from '../types/exercise'

interface DualNBackGridExerciseProps {
  exercise: ExerciseDto
  onComplete?: (result: ExerciseResult | number) => void
  showInstruction?: boolean
}

/**
 * Dual Grid N-Back: 3×3 grid with colored cells. User taps "Match Position" or "Match Color"
 * when that attribute matches N steps back.
 */
export function DualNBackGridExercise({ exercise, onComplete, showInstruction = true }: DualNBackGridExerciseProps) {
  const params = exercise.dualNBackGridParams ?? exercise.dualNbackGridParams
  if (!params || !params.sequence?.length) {
    return <p className="error">Invalid Dual Grid N-Back exercise: missing sequence.</p>
  }

  const [phase, setPhase] = useState<'intro' | 'playing' | 'done'>('intro')
  const [index, setIndex] = useState(0)
  const [userPositionMatches, setUserPositionMatches] = useState<Set<number>>(new Set())
  const [userColorMatches, setUserColorMatches] = useState<Set<number>>(new Set())
  const [showFeedback, setShowFeedback] = useState<'position' | 'color' | 'wrong' | null>(null)

  const intervalMs = 2500
  const matchPositionSet = new Set(params.matchPositionIndices ?? [])
  const matchColorSet = new Set(params.matchColorIndices ?? [])
  const n = params.n ?? 1
  const gridSize = params.gridSize ?? 3
  const cellCount = gridSize * gridSize

  const handlePositionMatch = useCallback(() => {
    if (phase !== 'playing') return
    setUserPositionMatches((prev) => new Set(prev).add(index))
    setShowFeedback(matchPositionSet.has(index) ? 'position' : 'wrong')
    setTimeout(() => setShowFeedback(null), 400)
  }, [phase, index, matchPositionSet])

  const handleColorMatch = useCallback(() => {
    if (phase !== 'playing') return
    setUserColorMatches((prev) => new Set(prev).add(index))
    setShowFeedback(matchColorSet.has(index) ? 'color' : 'wrong')
    setTimeout(() => setShowFeedback(null), 400)
  }, [phase, index, matchColorSet])

  useEffect(() => {
    if (phase !== 'playing') return
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'p' || e.key === 'P') { handlePositionMatch(); return }
      if (e.key === 'c' || e.key === 'C') { handleColorMatch(); return }
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [phase, handlePositionMatch, handleColorMatch])

  useEffect(() => {
    if (phase !== 'playing' || index >= params.sequence.length) return
    const t = setTimeout(() => setIndex((i) => i + 1), intervalMs)
    return () => clearTimeout(t)
  }, [phase, index, params.sequence.length, intervalMs])

  useEffect(() => {
    if (phase === 'playing' && index >= params.sequence.length) setPhase('done')
  }, [phase, index, params.sequence.length])

  const posHits = intersectionSize(userPositionMatches, matchPositionSet)
  const posMisses = matchPositionSet.size - posHits
  const posFalseAlarms = userPositionMatches.size - posHits
  const colorHits = intersectionSize(userColorMatches, matchColorSet)
  const colorMisses = matchColorSet.size - colorHits
  const colorFalseAlarms = userColorMatches.size - colorHits
  const totalTargets = matchPositionSet.size + matchColorSet.size
  const totalHits = posHits + colorHits
  const score = computeDualScore(userPositionMatches, userColorMatches, matchPositionSet, matchColorSet)
  const posAccuracy = matchPositionSet.size === 0 ? 100 : Math.round((posHits / matchPositionSet.size) * 100)
  const colorAccuracy = matchColorSet.size === 0 ? 100 : Math.round((colorHits / matchColorSet.size) * 100)
  const overallAccuracy = totalTargets === 0 ? 100 : Math.round((totalHits / totalTargets) * 100)

  useEffect(() => {
    if (phase === 'done') {
      const subscores: { label: string; value: string | number }[] = [
        { label: 'Position hits', value: `${posHits}/${matchPositionSet.size}` },
      ]
      if (posMisses > 0) subscores.push({ label: 'Position misses', value: posMisses })
      if (posFalseAlarms > 0) subscores.push({ label: 'Position false alarms', value: posFalseAlarms })
      subscores.push({ label: 'Position accuracy', value: `${posAccuracy}%` })
      subscores.push({ label: 'Color hits', value: `${colorHits}/${matchColorSet.size}` })
      if (colorMisses > 0) subscores.push({ label: 'Color misses', value: colorMisses })
      if (colorFalseAlarms > 0) subscores.push({ label: 'Color false alarms', value: colorFalseAlarms })
      subscores.push({ label: 'Color accuracy', value: `${colorAccuracy}%` })
      subscores.push({ label: 'Overall accuracy', value: `${overallAccuracy}%` })
      onComplete?.({ score, subscores })
    }
  }, [phase, onComplete, score, posHits, posMisses, posFalseAlarms, colorHits, colorMisses, colorFalseAlarms, matchPositionSet.size, matchColorSet.size, posAccuracy, colorAccuracy, overallAccuracy])
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
            Cells will light up with different colors. Tap &quot;Match Position&quot; when the same cell appears
            again, or &quot;Match Color&quot; when the same color appears, {params.n} step(s) back.
          </p>
        )}
        <button onClick={handleStart} className="nback-start-btn" autoFocus>
          Start
        </button>
      </div>
    )
  }

  if (phase === 'playing') {
    const stim = params.sequence[index]
    return (
      <div className="nback-playing">
        <div className="nback-n-badge" aria-label={`${n}-Back`}>
          {n}-Back
        </div>
        <div className="nback-grid-container">
          <div
            className="nback-grid nback-grid--dual"
            style={{ '--grid-size': gridSize } as React.CSSProperties}
          >
            {Array.from({ length: cellCount }, (_, i) => (
              <div
                key={i}
                className={`nback-grid-cell nback-grid-cell--dual ${
                  i === stim.position ? 'nback-grid-cell--active' : ''
                }`}
                style={
                  i === stim.position
                    ? ({ '--cell-color': stim.color } as React.CSSProperties)
                    : undefined
                }
              />
            ))}
          </div>
        </div>
        {showFeedback === 'position' && (
          <div className="nback-feedback nback-feedback--correct">Correct! (Position)</div>
        )}
        {showFeedback === 'color' && (
          <div className="nback-feedback nback-feedback--correct">Correct! (Color)</div>
        )}
        {showFeedback === 'wrong' && (
          <div className="nback-feedback nback-feedback--wrong">Incorrect</div>
        )}
        <div className="nback-dual-buttons">
          <button
            onClick={handlePositionMatch}
            className="nback-match-btn nback-match-btn--position"
            disabled={showFeedback !== null}
          >
            Match Position
          </button>
          <button
            onClick={handleColorMatch}
            className="nback-match-btn nback-match-btn--color"
            disabled={showFeedback !== null}
          >
            Match Color
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
        Score: {Math.round(score * 100)}% · Hits: {totalHits} / {totalTargets} (Position: {posHits}/
        {matchPositionSet.size}, Color: {colorHits}/{matchColorSet.size})
      </p>
    </div>
  )
}

function computeDualScore(
  userPos: Set<number>,
  userCol: Set<number>,
  matchPos: Set<number>,
  matchCol: Set<number>
): number {
  const totalTargets = matchPos.size + matchCol.size
  if (totalTargets === 0) return 1
  const hits = intersectionSize(userPos, matchPos) + intersectionSize(userCol, matchCol)
  const falseAlarms = (userPos.size - intersectionSize(userPos, matchPos)) + (userCol.size - intersectionSize(userCol, matchCol))
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
