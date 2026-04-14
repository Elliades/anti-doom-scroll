import { useState, useEffect, useRef } from 'react'
import type { ExerciseDto } from '../../types/api'

export interface GenericTextExerciseProps {
  exercise: ExerciseDto
  onComplete?: (score?: number) => void
}

/** Pick mobile keyboard from expected answers (digits vs decimal vs letters). */
function inferAnswerKeyboardMode(expectedAnswers: string[]): 'numeric' | 'decimal' | 'text' {
  const trimmed = expectedAnswers.map((a) => a.trim()).filter((a) => a.length > 0)
  if (trimmed.length === 0) return 'text'
  const isIntegerString = (s: string) => /^\d+$/.test(s)
  const isDecimalString = (s: string) => /^\d+\.\d+$/.test(s)
  const isIntegerOrDecimal = (s: string) => isIntegerString(s) || isDecimalString(s)
  if (trimmed.every(isIntegerString)) return 'numeric'
  if (trimmed.every(isIntegerOrDecimal)) return 'decimal'
  return 'text'
}

/**
 * Generic text-based exercise: prompt + input + check. Used for FLASHCARD_QA, CLOZE, etc.
 * Reused in SessionExerciseBlock and standalone play page. Calls onComplete(1) when answer is correct.
 */
export function GenericTextExercise({ exercise, onComplete }: GenericTextExerciseProps) {
  const [answer, setAnswer] = useState('')
  const [revealed, setRevealed] = useState(false)
  const inputRef = useRef<HTMLInputElement>(null)
  const keyboardMode = inferAnswerKeyboardMode(exercise.expectedAnswers)
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

  // Open the soft keyboard on mobile when the user can type (autoFocus alone is unreliable on iOS).
  useEffect(() => {
    if (revealed) return
    const id = window.setTimeout(() => {
      inputRef.current?.focus({ preventScroll: true })
    }, 10)
    return () => clearTimeout(id)
  }, [revealed])

  const inputMode =
    keyboardMode === 'numeric' ? 'numeric' : keyboardMode === 'decimal' ? 'decimal' : 'text'

  return (
    <>
      <p className="prompt">{exercise.prompt}</p>
      <div className="input-row">
        <input
          ref={inputRef}
          type="text"
          inputMode={inputMode}
          autoComplete="off"
          autoCorrect="off"
          spellCheck={false}
          autoCapitalize="off"
          enterKeyHint="done"
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
