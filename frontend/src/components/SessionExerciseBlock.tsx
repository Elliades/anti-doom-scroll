import { useEffect, useState } from 'react'
import type { SessionResponseDto, SessionStepDto } from '../types/api'
import type { ExerciseResult } from '../types/exercise'
import { ExercisePlayer } from './ExercisePlayer'
import { ScoreAnimation } from './ScoreAnimation'

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
  profileId: _profileId,
  elapsedSeconds,
  onDone,
  stepLabel,
}: SessionExerciseBlockProps) {
  const [stepIndex, setStepIndex] = useState(0)
  const [stepDone, setStepDone] = useState(false)
  const [lastResult, setLastResult] = useState<{ result: ExerciseResult; elapsedMs: number } | null>(null)

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

  const goNext = () => {
    setStepIndex((i) => i + 1)
    setStepDone(false)
    setLastResult(null)
  }

  useEffect(() => {
    if (stepDone && stepIndex < steps.length - 1) {
      const t = setTimeout(goNext, 600)
      return () => clearTimeout(t)
    }
  }, [stepDone, stepIndex, steps.length])

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
        {stepDone && lastResult ? (
          <>
            <ScoreAnimation
              score={lastResult.result.score}
              elapsedMs={lastResult.elapsedMs}
              subscores={lastResult.result.subscores}
            />
            <div className="session-info">
              <small>Session: {session.sessionDefaultSeconds}s default · Low battery: {session.lowBatteryModeSeconds}s</small>
            </div>
          </>
        ) : (
          <>
            <ExercisePlayer
              exercise={exercise}
              onComplete={(result, elapsed) => {
                setLastResult({ result, elapsedMs: elapsed ?? 0 })
                setStepDone(true)
              }}
            />
            <div className="session-info">
              <small>Session: {session.sessionDefaultSeconds}s default · Low battery: {session.lowBatteryModeSeconds}s</small>
            </div>
          </>
        )}
      </main>
      <footer className="footer">
        <button onClick={handlePrimaryAction} disabled={!stepDone}>
          {isLastStep ? 'Continue' : 'Next'}
        </button>
      </footer>
    </>
  )
}
