import { useEffect, useState } from 'react'
import type { SessionResponseDto, SessionStepDto } from '../types/api'
import { NBackExercise } from './NBackExercise'

function formatElapsed(seconds: number): string {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}:${s.toString().padStart(2, '0')}`
}

interface SessionExerciseBlockProps {
  session: SessionResponseDto
  profileId: string
  elapsedSeconds: number
  onDone: () => void
  stepLabel?: string
}

export function SessionExerciseBlock({
  session,
  profileId,
  elapsedSeconds,
  onDone,
  stepLabel,
}: SessionExerciseBlockProps) {
  const [stepIndex, setStepIndex] = useState(0)
  const [answer, setAnswer] = useState('')
  const [revealed, setRevealed] = useState(false)
  const [nbackDone, setNbackDone] = useState(false)

  const steps = session.steps
  if (steps.length === 0) {
    return (
      <>
        <p className="step">No exercises in this session.</p>
        <footer className="footer">
          <button onClick={onDone}>Continue</button>
        </footer>
      </>
    )
  }

  const step = steps[stepIndex] as SessionStepDto
  const exercise = step.exercise
  const isNBack = exercise.type === 'N_BACK'
  const isCorrect = !isNBack && exercise.expectedAnswers.some(
    (a) => a.trim().toLowerCase() === answer.trim().toLowerCase()
  )
  const canProceed = isNBack ? nbackDone : revealed

  const goNext = () => {
    setStepIndex((i) => i + 1)
    setAnswer('')
    setRevealed(false)
    setNbackDone(false)
  }

  const handleCheck = () => {
    if (revealed) return
    setRevealed(true)
    if (!isNBack && isCorrect && stepIndex < steps.length - 1) {
      setTimeout(goNext, 400)
    }
  }

  useEffect(() => {
    if (isNBack && nbackDone && stepIndex < steps.length - 1) {
      const t = setTimeout(goNext, 600)
      return () => clearTimeout(t)
    }
  }, [isNBack, nbackDone, stepIndex, steps.length])

  const isLastStep = stepIndex >= steps.length - 1
  const handlePrimaryAction = () => {
    if (isLastStep) onDone()
    else goNext()
  }

  return (
    <>
      <header className="header">
        {stepLabel && <span className="badge">{stepLabel}</span>}
        <span className="step">Step {step.stepIndex} of {steps.length}</span>
        <span className="timer" aria-label="Elapsed time">{formatElapsed(elapsedSeconds)}</span>
      </header>
      <main className="main">
        {isNBack ? (
          <NBackExercise exercise={exercise} onComplete={() => setNbackDone(true)} />
        ) : (
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
        )}
        <div className="session-info">
          <small>Session: {session.sessionDefaultSeconds}s default · Low battery: {session.lowBatteryModeSeconds}s</small>
        </div>
      </main>
      <footer className="footer">
        <button onClick={handlePrimaryAction} disabled={!canProceed}>
          {isLastStep ? 'Continue' : 'Next'}
        </button>
      </footer>
    </>
  )
}
