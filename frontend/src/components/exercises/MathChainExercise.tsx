import { useState, useEffect, useRef, useCallback } from 'react'
import type { ExerciseDto } from '../../types/api'
import type { ExerciseResult } from '../../types/exercise'
import { ComplexityBadge } from './ComplexityBadge'

export interface MathChainExerciseProps {
  exercise: ExerciseDto
  onComplete?: (result: ExerciseResult | number) => void
  showInstruction?: boolean
}

const OP_SYMBOLS: Record<string, string> = {
  ADD: '+',
  SUBTRACT: '−',
  MULTIPLY: '×',
  DIVIDE: '÷',
}

type Phase =
  | 'intro'
  | 'show_number'
  | 'show_step'
  | 'wait_continue'
  | 'ask_answer'
  | 'revealed'
  | 'done'

const SHOW_NUMBER_MS = 1500
const SHOW_STEP_MS = 1500

export function MathChainExercise({ exercise, onComplete, showInstruction }: MathChainExerciseProps) {
  const params = exercise.mathChainParams
  const steps = params?.steps ?? []
  const startNumber = params?.startNumber ?? 0
  const expectedAnswer = params?.expectedAnswer ?? 0

  const [phase, setPhase] = useState<Phase>('intro')
  const [stepIndex, setStepIndex] = useState(0)
  const [answer, setAnswer] = useState('')
  const [revealed, setRevealed] = useState(false)

  const completedRef = useRef(false)
  const inputRef = useRef<HTMLInputElement>(null)

  const currentStep = steps[stepIndex] ?? null
  const isCorrect = parseInt(answer.trim(), 10) === expectedAnswer
  const isLastStep = stepIndex >= steps.length - 1
  const complexityScore = params?.totalComplexity ?? null

  // Phase: show_number → auto-advance after delay
  useEffect(() => {
    if (phase !== 'show_number') return
    const t = setTimeout(() => {
      setStepIndex(0)
      setPhase('show_step')
    }, SHOW_NUMBER_MS)
    return () => clearTimeout(t)
  }, [phase])

  // Phase: show_step → auto-advance after delay to wait_continue or ask_answer
  useEffect(() => {
    if (phase !== 'show_step') return
    const t = setTimeout(() => {
      if (isLastStep) {
        setPhase('ask_answer')
      } else {
        setPhase('wait_continue')
      }
    }, SHOW_STEP_MS)
    return () => clearTimeout(t)
  }, [phase, isLastStep])

  // Focus input when ask_answer
  useEffect(() => {
    if (phase !== 'ask_answer') return
    const t = window.setTimeout(() => {
      inputRef.current?.focus({ preventScroll: true })
    }, 10)
    return () => clearTimeout(t)
  }, [phase])

  const handleStart = useCallback(() => {
    setPhase('show_number')
  }, [])

  const handleContinue = useCallback(() => {
    if (phase !== 'wait_continue') return
    const next = stepIndex + 1
    setStepIndex(next)
    setPhase('show_step')
  }, [phase, stepIndex])

  const handleCheck = useCallback(() => {
    if (phase !== 'ask_answer' || revealed) return
    setRevealed(true)
    setPhase('revealed')
  }, [phase, revealed])

  useEffect(() => {
    if (phase !== 'revealed') return
    if (completedRef.current) return
    completedRef.current = true
    const score = isCorrect ? 1 : 0
    const totalComplexity = params?.totalComplexity ?? 0
    const rounded = Math.round(totalComplexity * 10) / 10
    onComplete?.({
      score,
      subscores: [
        { label: 'Steps', value: steps.length },
        { label: 'Chain complexity', value: rounded },
      ],
    })
  }, [phase, isCorrect, onComplete, params?.totalComplexity, steps.length])

  // Keyboard: Enter to continue or check
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.ctrlKey || e.altKey || e.metaKey) return
      if (e.key === 'Enter') {
        if (phase === 'wait_continue') handleContinue()
        else if (phase === 'ask_answer') handleCheck()
      }
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [phase, handleContinue, handleCheck])

  if (!params || steps.length === 0) {
    return <p className="prompt">No math chain data available.</p>
  }

  // ── Intro ──
  if (phase === 'intro') {
    return (
      <div className="mc">
        <div className="mc-header">Mental Math Chain</div>
        {showInstruction !== false && (
          <div className="mc-instructions">
            <p>A number will appear, followed by operations one at a time.</p>
            <p>Compute each step mentally, then type the final result.</p>
          </div>
        )}
        <button type="button" className="mc-start-btn" onClick={handleStart} autoFocus>
          Start
        </button>
        {complexityScore == null ? null : <ComplexityBadge score={complexityScore} />}
      </div>
    )
  }

  // ── Show starting number ──
  if (phase === 'show_number') {
    return (
      <div className="mc">
        <div className="mc-header">Starting number</div>
        <div className="mc-number mc-number--appear">{startNumber}</div>
        <div className="mc-progress">
          <span className="mc-progress-text">Get ready…</span>
        </div>
        {complexityScore == null ? null : <ComplexityBadge score={complexityScore} />}
      </div>
    )
  }

  // ── Show step (operation reveal) ──
  if (phase === 'show_step' && currentStep) {
    const sym = OP_SYMBOLS[currentStep.operation] ?? '?'
    return (
      <div className="mc">
        <div className="mc-header">Step {stepIndex + 1} of {steps.length}</div>
        <div className="mc-operation mc-operation--appear">
          {sym} {currentStep.operand}
        </div>
        <div className="mc-progress">
          <div className="mc-progress-bar">
            <div
              className="mc-progress-fill"
              style={{ width: `${((stepIndex + 1) / steps.length) * 100}%` }}
            />
          </div>
        </div>
        {complexityScore == null ? null : <ComplexityBadge score={complexityScore} />}
      </div>
    )
  }

  // ── Wait for continue ──
  if (phase === 'wait_continue' && currentStep) {
    const sym = OP_SYMBOLS[currentStep.operation] ?? '?'
    return (
      <div className="mc">
        <div className="mc-header">Step {stepIndex + 1} of {steps.length}</div>
        <div className="mc-operation">
          {sym} {currentStep.operand}
        </div>
        <button
          type="button"
          className="mc-continue-btn"
          onClick={handleContinue}
          autoFocus
        >
          Continue →
        </button>
        <div className="mc-progress">
          <div className="mc-progress-bar">
            <div
              className="mc-progress-fill"
              style={{ width: `${((stepIndex + 1) / steps.length) * 100}%` }}
            />
          </div>
        </div>
        {complexityScore == null ? null : <ComplexityBadge score={complexityScore} />}
      </div>
    )
  }

  // ── Ask answer ──
  if (phase === 'ask_answer') {
    return (
      <div className="mc">
        <div className="mc-header">What's the result?</div>
        <div className="mc-chain-summary">
          <span className="mc-chain-start">{startNumber}</span>
          {steps.map((s, i) => {
            const sym = OP_SYMBOLS[s.operation] ?? '?'
            return (
              <span key={i} className="mc-chain-op">
                {' '}{sym} {s.operand}
              </span>
            )
          })}
          <span className="mc-chain-eq"> = ?</span>
        </div>
        <div className="input-row">
          <input
            ref={inputRef}
            type="text"
            inputMode="numeric"
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
          />
          <button onClick={handleCheck}>Check</button>
        </div>
        {complexityScore == null ? null : <ComplexityBadge score={complexityScore} />}
      </div>
    )
  }

  // ── Revealed / Done ──
  return (
    <div className="mc">
      <div className={`mc-header ${isCorrect ? 'mc-header--correct' : 'mc-header--fail'}`}>
        {isCorrect ? 'Correct!' : 'Wrong'}
      </div>
      <div className="mc-chain-summary">
        <span className="mc-chain-start">{startNumber}</span>
        {steps.map((s, i) => {
          const sym = OP_SYMBOLS[s.operation] ?? '?'
          return (
            <span key={i} className="mc-chain-op">
              {' '}{sym} {s.operand}
            </span>
          )
        })}
        <span className="mc-chain-eq"> = {expectedAnswer}</span>
      </div>
      <div className="input-row">
        <input
          type="text"
          value={answer}
          disabled
        />
        <span className={isCorrect ? 'correct' : 'incorrect'}>
          {isCorrect ? '✓ Correct' : `✗ Expected: ${expectedAnswer}`}
        </span>
      </div>
      {complexityScore == null ? null : <ComplexityBadge score={complexityScore} />}
    </div>
  )
}
