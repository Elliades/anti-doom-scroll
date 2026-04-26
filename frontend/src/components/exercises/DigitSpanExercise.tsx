import { useState, useCallback, useEffect, useRef, useMemo } from 'react'
import type { ExerciseDto } from '../../types/api'
import type { ExerciseResult } from '../../types/exercise'
import { estimateDigitSpanComplexityScore } from '../../api/exerciseParamGenerators'

export interface DigitSpanExerciseProps {
  exercise: ExerciseDto
  onComplete?: (result: ExerciseResult | number) => void
}

type ChallengeMode = 'forward' | 'ascending' | 'descending' | 'even_odd' | 'odd_even' | 'every_other'

const CHALLENGE_LABELS: Record<ChallengeMode, string> = {
  forward: 'Original order',
  ascending: 'Ascending',
  descending: 'Descending',
  even_odd: 'Even first',
  odd_even: 'Odd first',
  every_other: 'Every other (1st, 3rd…)',
}

const NUM_KEYBOARD_ROWS = [
  [1, 2, 3],
  [4, 5, 6],
  [7, 8, 9],
  [0],
]

function generateDigits(length: number): number[] {
  const out: number[] = []
  for (let i = 0; i < length; i++) out.push(Math.floor(Math.random() * 10))
  return out
}

function pickChallenge(digits: number[]): ChallengeMode {
  const modes: ChallengeMode[] = ['ascending', 'descending', 'even_odd', 'odd_even', 'every_other']
  const hasEven = digits.some((d) => d % 2 === 0)
  const hasOdd = digits.some((d) => d % 2 !== 0)
  const avail = modes.filter((m) => {
    if ((m === 'even_odd' || m === 'odd_even') && (!hasEven || !hasOdd)) return false
    return true
  })
  return avail[Math.floor(Math.random() * avail.length)]
}

function getExpected(digits: number[], mode: ChallengeMode): number[] {
  switch (mode) {
    case 'forward':      return [...digits]
    case 'ascending':    return [...digits].sort((a, b) => a - b)
    case 'descending':   return [...digits].sort((a, b) => b - a)
    case 'even_odd':     return [...digits.filter((d) => d % 2 === 0), ...digits.filter((d) => d % 2 !== 0)]
    case 'odd_even':     return [...digits.filter((d) => d % 2 !== 0), ...digits.filter((d) => d % 2 === 0)]
    case 'every_other':  return digits.filter((_, i) => i % 2 === 0)
  }
}

/*
 * Fluid single-screen digit span.
 *
 * Phase machine:
 *   intro → memorize (blink 3×) → goal_reveal ("Original order") → input
 *   correct → correct_flash (tiles green 0.5s)
 *     if forward: → goal_reveal (next challenge) → input
 *     if challenge: → memorize (next round, +1 digit)
 *   wrong → fail → done
 */

type Phase =
  | 'intro'
  | 'memorize'
  | 'goal_reveal'
  | 'input'
  | 'correct_flash'
  | 'fail'
  | 'done'

export function DigitSpanExercise({ exercise, onComplete }: DigitSpanExerciseProps) {
  const params = exercise.digitSpanParams
  const startLength = params?.startLength ?? 3
  const displayTimeMs = params?.displayTimeMs ?? 3000
  const maxLength = params?.maxLength ?? 15

  const [phase, setPhase] = useState<Phase>('intro')
  const [digits, setDigits] = useState<number[]>([])
  const [digitLength, setDigitLength] = useState(startLength)
  const [roundIndex, setRoundIndex] = useState(0)
  const [challenge, setChallenge] = useState<ChallengeMode>('forward')
  const [inputDigits, setInputDigits] = useState<number[]>([])
  const [blinkCount, setBlinkCount] = useState(0)
  const [feedback, setFeedback] = useState('')
  const [shaking, setShaking] = useState(false)
  const cognitiveLoad = Math.round(estimateDigitSpanComplexityScore(digitLength))

  const completedRef = useRef(false)
  const statsRef = useRef({ maxReached: 0, totalRounds: 0, totalChallenges: 0, challengesPassed: 0 })
  // What to do after correct_flash finishes
  const afterFlashRef = useRef<'challenge' | 'next_round'>('challenge')

  const expected = useMemo(() => getExpected(digits, challenge), [digits, challenge])
  const inputSlotCount = expected.length

  // ─── Start a memorize round ─────────────────────────────────

  const startMemorize = useCallback((length: number, rIdx: number) => {
    const d = generateDigits(length)
    setDigits(d)
    setDigitLength(length)
    setRoundIndex(rIdx)
    setChallenge('forward')
    setInputDigits([])
    setFeedback('')
    setBlinkCount(0)
    setPhase('memorize')
  }, [])

  // ─── Memorize blink timer ───────────────────────────────────

  useEffect(() => {
    if (phase !== 'memorize') return
    const totalBlinks = Math.ceil(displayTimeMs / 1000)
    if (blinkCount >= totalBlinks) {
      setPhase('goal_reveal')
      return
    }
    const t = setTimeout(() => setBlinkCount((c) => c + 1), 1000)
    return () => clearTimeout(t)
  }, [phase, blinkCount, displayTimeMs])

  // ─── Goal reveal → input ────────────────────────────────────

  useEffect(() => {
    if (phase !== 'goal_reveal') return
    const t = setTimeout(() => {
      setInputDigits([])
      setPhase('input')
    }, 1500)
    return () => clearTimeout(t)
  }, [phase])

  // ─── Correct flash → next action ────────────────────────────

  useEffect(() => {
    if (phase !== 'correct_flash') return
    const t = setTimeout(() => {
      if (afterFlashRef.current === 'challenge') {
        const next = pickChallenge(digits)
        setChallenge(next)
        setInputDigits([])
        setPhase('goal_reveal')
      } else {
        const nextLen = digitLength + 1
        if (nextLen > maxLength) {
          finishExercise()
          return
        }
        startMemorize(nextLen, roundIndex + 1)
      }
    }, 700)
    return () => clearTimeout(t)
  }, [phase, digits, digitLength, maxLength, roundIndex, startMemorize])

  // ─── Numpad input ───────────────────────────────────────────

  const handleDigitPress = useCallback((d: number) => {
    if (phase !== 'input') return
    setInputDigits((prev) => prev.length < inputSlotCount ? [...prev, d] : prev)
  }, [phase, inputSlotCount])

  const handleBackspace = useCallback(() => {
    if (phase !== 'input') return
    setInputDigits((prev) => prev.slice(0, -1))
  }, [phase])

  const handleSubmit = useCallback(() => {
    if (phase !== 'input') return
    if (inputDigits.length !== inputSlotCount) {
      setShaking(true)
      setTimeout(() => setShaking(false), 500)
      return
    }

    const correct = inputDigits.every((d, i) => d === expected[i])

    if (challenge === 'forward') {
      statsRef.current.totalRounds++
      if (correct) {
        statsRef.current.maxReached = Math.max(statsRef.current.maxReached, digitLength)
        afterFlashRef.current = 'challenge'
        setPhase('correct_flash')
      } else {
        statsRef.current.maxReached = Math.max(statsRef.current.maxReached, digitLength - 1)
        setFeedback(`Expected: ${expected.join(' ')}`)
        setPhase('fail')
      }
    } else {
      statsRef.current.totalChallenges++
      if (correct) {
        statsRef.current.challengesPassed++
        afterFlashRef.current = 'next_round'
        setPhase('correct_flash')
      } else {
        setFeedback(`Expected: ${expected.join(' ')}`)
        setPhase('fail')
      }
    }
  }, [phase, inputDigits, inputSlotCount, expected, challenge, digitLength])

  // Physical keyboard
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.ctrlKey || e.altKey || e.metaKey) return
      if (phase !== 'input') return
      if (e.key >= '0' && e.key <= '9') handleDigitPress(parseInt(e.key))
      else if (e.key === 'Backspace') handleBackspace()
      else if (e.key === 'Enter') handleSubmit()
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [phase, handleDigitPress, handleBackspace, handleSubmit])

  // ─── Finish ─────────────────────────────────────────────────

  const finishExercise = useCallback(() => {
    if (completedRef.current) return
    completedRef.current = true
    setPhase('done')
    const s = statsRef.current
    const rawScore = s.maxReached <= startLength ? 0 : (s.maxReached - startLength) / (maxLength - startLength)
    onComplete?.({
      score: Math.max(0, Math.min(1, rawScore)),
      subscores: [
        { label: 'Max span', value: s.maxReached },
        { label: 'Rounds', value: s.totalRounds },
        { label: 'Challenges passed', value: `${s.challengesPassed}/${s.totalChallenges}` },
      ],
    })
  }, [startLength, maxLength, onComplete])

  // ─── Render ─────────────────────────────────────────────────

  if (phase === 'intro') {
    return (
      <div className="ds">
        <p className="cognitive-load-badge">Charge Cognitive : {cognitiveLoad}/100</p>
        <div className="ds-header">Digit Span</div>
        <div className="ds-instructions">
          <p>Digits will flash on screen — memorize them!</p>
          <p>Recall in order, then take on a challenge.</p>
          <p>The sequence grows with each success.</p>
        </div>
        <button type="button" className="ds-start-btn" onClick={() => startMemorize(startLength, 0)} autoFocus>
          Start
        </button>
      </div>
    )
  }

  if (phase === 'done') {
    return (
      <div className="ds">
        <p className="cognitive-load-badge">Charge Cognitive : {cognitiveLoad}/100</p>
        <div className="ds-header">Done!</div>
        <p className="ds-stats">Max span: {statsRef.current.maxReached} digits</p>
      </div>
    )
  }

  if (phase === 'fail') {
    return (
      <div className="ds">
        <p className="cognitive-load-badge">Charge Cognitive : {cognitiveLoad}/100</p>
        <div className="ds-header ds-header--fail">Wrong!</div>
        <div className="ds-tiles">
          {expected.map((d, i) => (
            <div key={i} className="ds-tile ds-tile--fail">{d}</div>
          ))}
        </div>
        <p className="ds-fail-text">{feedback}</p>
        <p className="ds-stats">Max span: {statsRef.current.maxReached}</p>
        <button type="button" className="ds-end-btn" onClick={finishExercise}>
          End Exercise
        </button>
      </div>
    )
  }

  // ── Active gameplay phases (memorize / goal_reveal / input / correct_flash) ──

  const isMemorizing = phase === 'memorize'
  const showGoal = phase === 'goal_reveal'
  const showInput = phase === 'input' || phase === 'correct_flash'
  const isLastBlink = isMemorizing && blinkCount >= Math.ceil(displayTimeMs / 1000) - 1

  return (
    <div className="ds">
      <p className="cognitive-load-badge">Charge Cognitive : {cognitiveLoad}/100</p>
      {/* Header */}
      <div className={`ds-header ${phase === 'correct_flash' ? 'ds-header--correct' : ''}`}>
        {isMemorizing ? 'Memorize' : 'List them'}
      </div>

      <div className="ds-span-badge">{digitLength} digits</div>

      <div className="ds-stage">
        {/* Memorize: digit tiles blinking */}
        {isMemorizing && (
          <div key={`mem-${roundIndex}`} className={`ds-tiles ds-tiles--memorize ${isLastBlink ? 'ds-blink-last' : 'ds-blink'}`}>
            {digits.map((d, i) => (
              <div key={i} className="ds-tile ds-tile--show">{d}</div>
            ))}
          </div>
        )}

        {/* Goal reveal */}
        {showGoal && (
          <div key={`goal-${challenge}`} className="ds-goal-reveal">
            <span className="ds-goal-label">{CHALLENGE_LABELS[challenge]}</span>
          </div>
        )}

        {/* Input tiles + numpad */}
        {showInput && (
          <div key={`inp-${challenge}`} className="ds-input-area">
            <div className="ds-challenge-tag">{CHALLENGE_LABELS[challenge]}</div>

            <div className={`ds-tiles ${shaking ? 'ds-shake' : ''} ${phase === 'correct_flash' ? 'ds-tiles--correct' : ''}`}>
              {Array.from({ length: inputSlotCount }, (_, i) => {
                const filled = i < inputDigits.length
                const isActive = i === inputDigits.length && phase === 'input'
                return (
                  <div
                    key={i}
                    className={`ds-tile ${filled ? 'ds-tile--filled' : ''} ${isActive ? 'ds-tile--active' : ''} ${phase === 'correct_flash' && filled ? 'ds-tile--correct' : ''}`}
                  >
                    {filled ? inputDigits[i] : ''}
                  </div>
                )
              })}
            </div>

            {phase === 'input' && (
              <div className="ds-keyboard">
                {NUM_KEYBOARD_ROWS.map((row, rIdx) => (
                  <div key={rIdx} className="ds-keyboard-row">
                    {rIdx === NUM_KEYBOARD_ROWS.length - 1 && (
                      <button type="button" className="ds-key ds-key--action" onClick={handleBackspace}>⌫</button>
                    )}
                    {row.map((d) => (
                      <button key={d} type="button" className="ds-key ds-key--digit" onClick={() => handleDigitPress(d)}>
                        {d}
                      </button>
                    ))}
                    {rIdx === NUM_KEYBOARD_ROWS.length - 1 && (
                      <button type="button" className="ds-key ds-key--action ds-key--enter" onClick={handleSubmit}>✓</button>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  )
}
