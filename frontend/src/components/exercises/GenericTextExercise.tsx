import { useState, useEffect } from 'react'
import type { ExerciseDto } from '../../types/api'

export interface GenericTextExerciseProps {
  exercise: ExerciseDto
  onComplete?: (score?: number) => void
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
    if (revealed) onComplete?.(isCorrect ? 1 : 0)
  }, [revealed, isCorrect, onComplete])

  return (
    <>
      <p className="prompt">{exercise.prompt}</p>
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
