import { useRef, useState } from 'react'
import type { ExerciseDto } from '../../types/api'
import type { ExerciseResult, SubscoreDetail } from '../../types/exercise'

/** Scoring params: precision vs time weight and time thresholds (seconds). */
const EPS = 1
const P = 1
const T_GOOD_SEC = 15
const T_FAIL_SEC = 60
const W = 0.7
/** Minimum relative error tolerance (20%) — never stricter than this. */
const TAU_MIN = 0.20

function clamp(x: number, lo: number, hi: number): number {
  return Math.max(lo, Math.min(hi, x))
}

/**
 * Compute estimation score from user answer and response time.
 * R = true result, A = user answer, t = response time (seconds).
 * tau = relative error at which precision reaches 0 (from toleranceFactor: tau = toleranceFactor - 1).
 */
export function computeEstimationScore(
  correctAnswer: number,
  userAnswer: number,
  responseTimeSeconds: number,
  toleranceFactor: number,
  options?: { eps?: number; tau?: number; p?: number; tGood?: number; tFail?: number; w?: number }
): { score: number; S_prec: number; S_time: number; S_global: number; band: 'success' | 'medium' | 'fail' } {
  const eps = options?.eps ?? EPS
  const tau = options?.tau ?? Math.max(TAU_MIN, toleranceFactor - 1)
  const p = options?.p ?? P
  const tGood = options?.tGood ?? T_GOOD_SEC
  const tFail = options?.tFail ?? T_FAIL_SEC
  const w = options?.w ?? W

  const e = Math.abs(userAnswer - correctAnswer) / Math.max(Math.abs(correctAnswer), eps)
  const S_prec = 100 * Math.max(0, 1 - Math.pow(e / tau, p))
  const S_time = 100 * clamp((tFail - responseTimeSeconds) / (tFail - tGood), 0, 1)
  const S_global = w * S_prec + (1 - w) * S_time
  const score = S_global / 100

  let band: 'success' | 'medium' | 'fail'
  if (S_global >= 75) band = 'success'
  else if (S_global >= 41) band = 'medium'
  else band = 'fail'

  return { score, S_prec, S_time, S_global, band }
}

export interface EstimationExerciseProps {
  exercise: ExerciseDto
  onComplete?: (result?: ExerciseResult | number) => void
  showInstruction?: boolean
}

/**
 * Estimation exercise: numeric input, scored by relative error and response time.
 * Displays precision, time, overall score and result band (Success / Medium / Fail).
 */
export function EstimationExercise({ exercise, onComplete }: EstimationExerciseProps) {
  const params = exercise.estimationParams
  const [answer, setAnswer] = useState('')
  const [revealed, setRevealed] = useState(false)
  const [lastSubscores, setLastSubscores] = useState<SubscoreDetail[] | null>(null)
  const startMsRef = useRef(Date.now())

  if (!params) {
    return (
      <p className="error">This estimation exercise is missing parameters.</p>
    )
  }

  const { correctAnswer, unit, toleranceFactor, hint, timeWeightHigher } = params
  /** Pure math: time matters more (w=0.4); word/geography: precision matters more (w=0.7). */
  const w = timeWeightHigher ? 0.4 : 0.7
  const numAnswer = Number(answer.trim().replace(/,/g, ''))
  const isValidNumber = answer.trim() !== '' && !Number.isNaN(numAnswer) && numAnswer > 0

  const handleCheck = () => {
    if (revealed || !isValidNumber) return
    setRevealed(true)
    const responseTimeSeconds = (Date.now() - startMsRef.current) / 1000
    const { score, S_prec, S_time, S_global, band } = computeEstimationScore(
      correctAnswer,
      numAnswer,
      responseTimeSeconds,
      toleranceFactor,
      { w }
    )
    const subscores: SubscoreDetail[] = [
      { label: 'Precision', value: `${Math.round(S_prec)}%` },
      { label: 'Time', value: `${Math.round(S_time)}%` },
      { label: 'Overall', value: `${Math.round(S_global)}%` },
      { label: 'Result', value: band === 'success' ? 'Success' : band === 'medium' ? 'Medium' : 'Fail' },
    ]
    setLastSubscores(subscores)
    onComplete?.({ score, subscores })
  }

  return (
    <>
      <p className="prompt">{exercise.prompt}</p>
      {hint != null && hint !== '' && (
        <p className="estimation-hint" aria-label="Hint">{hint}</p>
      )}
      <div className="input-row">
        <input
          type="text"
          inputMode="decimal"
          value={answer}
          onChange={(e) => setAnswer(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleCheck()}
          placeholder={unit ? `Your answer (${unit})` : 'Your answer'}
          autoFocus
          disabled={revealed}
          aria-label="Your numeric estimate"
        />
        {unit && <span className="estimation-unit">{unit}</span>}
        {!revealed ? (
          <button onClick={handleCheck} disabled={!isValidNumber}>Check</button>
        ) : (
          <span className={revealed ? 'correct' : ''}>✓ Checked</span>
        )}
      </div>
      {revealed && lastSubscores && (
        <ul className="estimation-subscores" aria-label="Score breakdown">
          {lastSubscores.map((s, i) => (
            <li key={i}>
              <span className="score-subscore-label">{s.label}: </span>
              <span className="score-subscore-value">{s.value}</span>
            </li>
          ))}
        </ul>
      )}
    </>
  )
}
