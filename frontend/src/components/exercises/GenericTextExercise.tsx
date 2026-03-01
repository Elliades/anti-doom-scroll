import { useState, useEffect } from 'react'
import type { ExerciseDto } from '../../types/api'
import type { ExerciseResult } from '../../types/exercise'

export interface GenericTextExerciseProps {
  exercise: ExerciseDto
  onComplete?: (result?: ExerciseResult | number) => void
  /** Ignored (no instruction block); kept for consistent ExercisePlayer prop shape. */
  showInstruction?: boolean
}

/**
 * Generic text-based exercise: prompt + input + check. Used for FLASHCARD_QA, CLOZE, etc.
 * Reused in SessionExerciseBlock and standalone play page. Calls onComplete(1) when answer is correct.
 */
export function GenericTextExercise({ exercise, onComplete }: GenericTextExerciseProps) {
  const [answer, setAnswer] = useState('')
  const [revealed, setRevealed] = useState(false)
  const isCorrect = exercise.expectedAnswers.some(
    (a) => a.trim().toLowerCase() === answer.trim().toLowerCase()
  )

  const handleCheck = () => {
    if (revealed) return
    setRevealed(true)
  }

  useEffect(() => {
    if (!revealed) return
    const score = isCorrect ? 1 : 0
    if (exercise.mathOperation != null && exercise.mathComplexityScore != null) {
      const rounded = Math.round(exercise.mathComplexityScore * 10) / 10
      onComplete?.({ score, subscores: [{ label: 'Problem complexity', value: rounded }] })
    } else {
      onComplete?.(score)
    }
  }, [revealed, isCorrect, onComplete, exercise.mathOperation, exercise.mathComplexityScore])

  return (
    <>
      <p className="prompt">{exercise.prompt}</p>
      {exercise.mathOperation != null && exercise.mathComplexityScore != null && (
        <p className="math-complexity" aria-label="Problem complexity score">
          Complexity: {Math.round(exercise.mathComplexityScore * 10) / 10}
        </p>
      )}
      <div className="input-row">
        <input
          type="text"
          value={answer}
          onChange={(e) => setAnswer(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleCheck()}
          placeholder="Your answer"
          autoFocus
          disabled={revealed}
        />
        {!revealed ? (
          <button onClick={handleCheck}>Check</button>
        ) : (
          <span className={isCorrect ? 'correct' : 'incorrect'}>
            {isCorrect ? '✓ Correct' : `✗ Expected: ${exercise.expectedAnswers[0] ?? '—'}`}
          </span>
        )}
      </div>
    </>
  )
}
