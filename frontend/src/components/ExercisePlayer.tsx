import { useEffect, useRef, useState } from 'react'
import type React from 'react'
import type { ExerciseDto } from '../types/api'
import { toExerciseResult, type ExerciseResult } from '../types/exercise'
import { Chronometer } from './Chronometer'
import { DualNBackCardExercise } from './DualNBackCardExercise'
import { DualNBackGridExercise } from './DualNBackGridExercise'
import { NBackExercise } from './NBackExercise'
import { NBackGridExercise } from './NBackGridExercise'
import { AnagramExercise } from './exercises/AnagramExercise'
import { WordleExercise } from './exercises/WordleExercise'
import { GenericTextExercise } from './exercises/GenericTextExercise'
import { MemoryCardExercise } from './exercises/MemoryCardExercise'
import { ImagePairExercise } from './exercises/ImagePairExercise'
import { SumPairExercise } from './exercises/SumPairExercise'

export interface ExercisePlayerProps {
  exercise: ExerciseDto
  onComplete?: (result: ExerciseResult, elapsedMs?: number) => void
}

/** Exercise components accept onComplete with ExerciseResult | number for backward compat */
type ExerciseCompleteHandler = (result: ExerciseResult | number) => void

const EXERCISE_TYPE_COMPONENTS: Record<string, React.ComponentType<{ exercise: ExerciseDto; onComplete?: ExerciseCompleteHandler }>> = {
  N_BACK: ({ exercise, onComplete }) => (
    <NBackExercise exercise={exercise} onComplete={onComplete} />
  ),
  N_BACK_GRID: ({ exercise, onComplete }) => (
    <NBackGridExercise exercise={exercise} onComplete={onComplete} />
  ),
  DUAL_NBACK_GRID: ({ exercise, onComplete }) => (
    <DualNBackGridExercise exercise={exercise} onComplete={onComplete} />
  ),
  DUAL_NBACK_CARD: ({ exercise, onComplete }) => (
    <DualNBackCardExercise exercise={exercise} onComplete={onComplete} />
  ),
  MEMORY_CARD_PAIRS: ({ exercise, onComplete }) => (
    <MemoryCardExercise exercise={exercise} onComplete={onComplete} />
  ),
  SUM_PAIR: ({ exercise, onComplete }) => (
    <SumPairExercise exercise={exercise} onComplete={onComplete} />
  ),
  IMAGE_PAIR: ({ exercise, onComplete }) => (
    <ImagePairExercise exercise={exercise} onComplete={onComplete} />
  ),
  ANAGRAM: ({ exercise, onComplete }) => (
    <AnagramExercise exercise={exercise} onComplete={onComplete} />
  ),
  WORDLE: ({ exercise, onComplete }) => (
    <WordleExercise exercise={exercise} onComplete={onComplete} />
  ),
}

/**
 * Pure exercise runner: renders the exercise and reports result via onComplete.
 * Result display is decoupled — the parent (LadderSessionBlock, SessionExerciseBlock,
 * PlayExercisePage) owns showing the score and action buttons.
 */
export function ExercisePlayer({ exercise, onComplete }: ExercisePlayerProps) {
  const [elapsedMs, setElapsedMs] = useState(0)
  const completedRef = useRef(false)
  const startRef = useRef(Date.now())

  useEffect(() => {
    if (completedRef.current) return
    const id = setInterval(() => setElapsedMs(Date.now() - startRef.current), 100)
    return () => clearInterval(id)
  }, [])

  const handleComplete = (input: ExerciseResult | number | undefined) => {
    if (completedRef.current) return
    completedRef.current = true
    const elapsed = Date.now() - startRef.current
    const result = toExerciseResult(input)
    onComplete?.(result, elapsed)
  }

  const Component = EXERCISE_TYPE_COMPONENTS[exercise.type]
  const ExerciseComponent = Component ?? GenericTextExercise

  return (
    <div className="exercise-with-chronometer">
      <div className="exercise-chronometer-row">
        <Chronometer elapsedMs={elapsedMs} className="timer" />
      </div>
      <ExerciseComponent
        exercise={exercise}
        onComplete={handleComplete}
      />
    </div>
  )
}
