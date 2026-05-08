import { useState, useEffect, useRef, useCallback } from 'react'
import type { ExerciseDto } from '../../types/api'
import type { ExerciseResult } from '../../types/exercise'

export interface RememberNumberExerciseProps {
  exercise: ExerciseDto
  onComplete?: (result: ExerciseResult | number) => void
  showInstruction?: boolean
}

type Phase = 'intro' | 'memorize' | 'math' | 'recall' | 'revealed'

export function RememberNumberExercise({ exercise, onComplete, showInstruction }: RememberNumberExerciseProps) {
  const params = exercise.rememberNumberParams
  const numberToRemember = params?.numberToRemember ?? 0
  const displayTimeMs = params?.displayTimeMs ?? 3000
  const mathPrompt = params?.mathPrompt ?? ''
  const mathExpectedAnswer = params?.mathExpectedAnswer ?? ''
  const mathComplexityScore = params?.mathComplexityScore ?? null

  const [phase, setPhase] = useState<Phase>('intro')
  const [mathAnswer, setMathAnswer] = useState('')
  const [recallAnswer, setRecallAnswer] = useState('')
  const [countdown, setCountdown] = useState(0)

  const completedRef = useRef(false)
  const mathInputRef = useRef<HTMLInputElement>(null)
  const recallInputRef = useRef<HTMLInputElement>(null)

  const isMathCorrect = mathAnswer.trim() === mathExpectedAnswer
  const isRecallCorrect = recallAnswer.trim() === numberToRemember.toString()

  const handleStart = useCallback(() => {
    setPhase('memorize')
    setCountdown(Math.ceil(displayTimeMs / 1000))
  }, [displayTimeMs])

  useEffect(() => {
    if (phase !== 'memorize') return
    const start = Date.now()
    const total = displayTimeMs
    const tick = setInterval(() => {
      const elapsed = Date.now() - start
      const remaining = Math.max(0, Math.ceil((total - elapsed) / 1000))
      setCountdown(remaining)
      if (elapsed >= total) {
        clearInterval(tick)
        setPhase('math')
      }
    }, 100)
    return () => clearInterval(tick)
  }, [phase, displayTimeMs])

  useEffect(() => {
    if (phase !== 'math') return
    const t = window.setTimeout(() => {
      mathInputRef.current?.focus({ preventScroll: true })
    }, 10)
    return () => clearTimeout(t)
  }, [phase])

  useEffect(() => {
    if (phase !== 'recall') return
    const t = window.setTimeout(() => {
      recallInputRef.current?.focus({ preventScroll: true })
    }, 10)
    return () => clearTimeout(t)
  }, [phase])

  const handleMathSubmit = useCallback(() => {
    if (phase !== 'math') return
    setPhase('recall')
  }, [phase])

  const handleRecallSubmit = useCallback(() => {
    if (phase !== 'recall') return
    setPhase('revealed')
  }, [phase])

  useEffect(() => {
    if (phase !== 'revealed') return
    if (completedRef.current) return
    completedRef.current = true

    const score = (isRecallCorrect && isMathCorrect) ? 1.0 : 0.0

    const subscores = [
      { label: 'Number recall', value: isRecallCorrect ? 'Correct' : 'Wrong' },
      { label: 'Math answer', value: isMathCorrect ? 'Correct' : 'Wrong' },
    ]
    if (mathComplexityScore != null) {
      subscores.push({ label: 'Problem complexity', value: Math.round(mathComplexityScore * 10) / 10 as unknown as string })
    }
    onComplete?.({ score, subscores })
  }, [phase, isRecallCorrect, isMathCorrect, onComplete, mathComplexityScore])

  if (!params) {
    return <p className="prompt">No remember-number data available.</p>
  }

  // ── Intro ──
  if (phase === 'intro') {
    return (
      <div className="mc">
        <div className="mc-header">Remember &amp; Recall</div>
        {showInstruction !== false && (
          <div className="mc-instructions">
            <p>A number will appear for a few seconds. Memorize it!</p>
            <p>Then solve a math problem. After that, type the number you memorized.</p>
          </div>
        )}
        <button type="button" className="mc-start-btn" onClick={handleStart} autoFocus>
          Start
        </button>
      </div>
    )
  }

  // ── Memorize ──
  if (phase === 'memorize') {
    return (
      <div className="mc">
        <div className="mc-header">Memorize this number</div>
        <div className="mc-number mc-number--appear">{numberToRemember}</div>
        <div className="mc-progress">
          <span className="mc-progress-text">{countdown}s remaining</span>
          <div className="mc-progress-bar">
            <div
              className="mc-progress-fill rn-countdown"
              style={{ width: `${(countdown / Math.ceil(displayTimeMs / 1000)) * 100}%` }}
            />
          </div>
        </div>
      </div>
    )
  }

  // ── Math distraction ──
  if (phase === 'math') {
    return (
      <div className="mc">
        <div className="mc-header">Solve this problem</div>
        <p className="prompt">{mathPrompt}</p>
        <div className="input-row">
          <input
            ref={mathInputRef}
            type="text"
            inputMode="numeric"
            autoComplete="off"
            autoCorrect="off"
            spellCheck={false}
            autoCapitalize="off"
            enterKeyHint="done"
            value={mathAnswer}
            onChange={(e) => setMathAnswer(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleMathSubmit()}
            placeholder="Math answer"
            autoFocus
          />
          <button onClick={handleMathSubmit}>Submit</button>
        </div>
      </div>
    )
  }

  // ── Recall ──
  if (phase === 'recall') {
    return (
      <div className="mc">
        <div className="mc-header">What was the number?</div>
        <div className="mc-instructions">
          <p>Type the number you memorized earlier.</p>
        </div>
        <div className="input-row">
          <input
            ref={recallInputRef}
            type="text"
            inputMode="numeric"
            autoComplete="off"
            autoCorrect="off"
            spellCheck={false}
            autoCapitalize="off"
            enterKeyHint="done"
            value={recallAnswer}
            onChange={(e) => setRecallAnswer(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleRecallSubmit()}
            placeholder="Remembered number"
            autoFocus
          />
          <button onClick={handleRecallSubmit}>Check</button>
        </div>
      </div>
    )
  }

  // ── Revealed ──
  return (
    <div className="mc">
      <div className={`mc-header ${isRecallCorrect ? 'mc-header--correct' : 'mc-header--fail'}`}>
        {isRecallCorrect && isMathCorrect
          ? 'Perfect!'
          : isRecallCorrect
            ? 'Number recalled!'
            : 'Wrong recall'}
      </div>

      <div className="rn-results">
        <div className="rn-result-row">
          <span className="rn-result-label">Number to remember:</span>
          <span className="rn-result-value">{numberToRemember}</span>
        </div>
        <div className="rn-result-row">
          <span className="rn-result-label">Your recall:</span>
          <span className={`rn-result-value ${isRecallCorrect ? 'correct' : 'incorrect'}`}>
            {recallAnswer || '—'} {isRecallCorrect ? '✓' : '✗'}
          </span>
        </div>
        <div className="rn-result-row">
          <span className="rn-result-label">Math ({mathPrompt}):</span>
          <span className={`rn-result-value ${isMathCorrect ? 'correct' : 'incorrect'}`}>
            {mathAnswer || '—'} {isMathCorrect ? '✓' : `✗ (${mathExpectedAnswer})`}
          </span>
        </div>
      </div>
    </div>
  )
}
