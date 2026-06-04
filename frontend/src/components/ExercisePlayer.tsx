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
import { EstimationExercise } from './exercises/EstimationExercise'
import { MemoryCardExercise } from './exercises/MemoryCardExercise'
import { ImagePairExercise } from './exercises/ImagePairExercise'
import { SumPairExercise } from './exercises/SumPairExercise'
import { DigitSpanExercise } from './exercises/DigitSpanExercise'
import { MathChainExercise } from './exercises/MathChainExercise'
import { RememberNumberExercise } from './exercises/RememberNumberExercise'

export interface ExercisePlayerProps {
  exercise: ExerciseDto
  /** When false, exercise intro screens omit the instruction paragraph (e.g. ladder: show only on first exercise). Default true. */
  showInstruction?: boolean
  onComplete?: (result: ExerciseResult, elapsedMs?: number) => void
  /** Optional skip action rendered next to the timer. */
  onSkip?: () => void
}

/** Exercise components accept onComplete with ExerciseResult | number for backward compat */
type ExerciseCompleteHandler = (result?: ExerciseResult | number) => void

type ExerciseComponentProps = {
  exercise: ExerciseDto
  onComplete?: ExerciseCompleteHandler
  showInstruction?: boolean
}

const EXERCISE_TYPE_COMPONENTS: Record<string, React.ComponentType<ExerciseComponentProps>> = {
  N_BACK: ({ exercise, onComplete, showInstruction }) => (
    <NBackExercise exercise={exercise} onComplete={onComplete} showInstruction={showInstruction} />
  ),
  N_BACK_GRID: ({ exercise, onComplete, showInstruction }) => (
    <NBackGridExercise exercise={exercise} onComplete={onComplete} showInstruction={showInstruction} />
  ),
  DUAL_NBACK_GRID: ({ exercise, onComplete, showInstruction }) => (
    <DualNBackGridExercise exercise={exercise} onComplete={onComplete} showInstruction={showInstruction} />
  ),
  DUAL_NBACK_CARD: ({ exercise, onComplete, showInstruction }) => (
    <DualNBackCardExercise exercise={exercise} onComplete={onComplete} showInstruction={showInstruction} />
  ),
  MEMORY_CARD_PAIRS: ({ exercise, onComplete, showInstruction }) => (
    <MemoryCardExercise exercise={exercise} onComplete={onComplete} showInstruction={showInstruction} />
  ),
  SUM_PAIR: ({ exercise, onComplete, showInstruction }) => (
    <SumPairExercise exercise={exercise} onComplete={onComplete} showInstruction={showInstruction} />
  ),
  IMAGE_PAIR: ({ exercise, onComplete, showInstruction }) => (
    <ImagePairExercise exercise={exercise} onComplete={onComplete} showInstruction={showInstruction} />
  ),
  ANAGRAM: ({ exercise, onComplete, showInstruction }) => (
    <AnagramExercise exercise={exercise} onComplete={onComplete} showInstruction={showInstruction} />
  ),
  WORDLE: ({ exercise, onComplete }) => (
    <WordleExercise exercise={exercise} onComplete={onComplete} />
  ),
  ESTIMATION: ({ exercise, onComplete, showInstruction }) => (
    <EstimationExercise exercise={exercise} onComplete={onComplete} showInstruction={showInstruction} />
  ),
  DIGIT_SPAN: ({ exercise, onComplete }) => (
    <DigitSpanExercise exercise={exercise} onComplete={onComplete} />
  ),
  MATH_CHAIN: ({ exercise, onComplete, showInstruction }) => (
    <MathChainExercise exercise={exercise} onComplete={onComplete} showInstruction={showInstruction} />
  ),
  REMEMBER_NUMBER: ({ exercise, onComplete, showInstruction }) => (
    <RememberNumberExercise exercise={exercise} onComplete={onComplete} showInstruction={showInstruction} />
  ),
}

/**
 * Pure exercise runner: renders the exercise and reports result via onComplete.
 * Result display is decoupled — the parent (LadderSessionBlock, SessionExerciseBlock,
 * PlayExercisePage) owns showing the score and action buttons.
 */
export function ExercisePlayer({ exercise, showInstruction = true, onComplete, onSkip }: ExercisePlayerProps) {
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
        {onSkip && (
          <button type="button" className="ladder-skip-btn" onClick={onSkip} aria-label="Skip exercise">
            Skip ▸
          </button>
        )}
        <Chronometer elapsedMs={elapsedMs} className="timer" />
      </div>
      <ExerciseComponent
        exercise={exercise}
        showInstruction={showInstruction}
        onComplete={handleComplete}
      />
    </div>
  )
}
