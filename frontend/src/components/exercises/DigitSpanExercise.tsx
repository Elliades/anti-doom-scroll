import { useState, useCallback, useEffect, useRef, useMemo } from 'react'
import type { ExerciseDto } from '../../types/api'
import type { ExerciseResult } from '../../types/exercise'

export interface DigitSpanExerciseProps {
  exercise: ExerciseDto
  onComplete?: (result: ExerciseResult | number) => void
}

type ChallengeMode = 'ascending' | 'descending' | 'even_odd' | 'odd_even' | 'every_other'

interface RoundState {
  digits: number[]
  length: number
  roundIndex: number
}

type Phase =
  | 'intro'
  | 'showing'
  | 'hiding'
  | 'forward_input'
  | 'forward_correct'
  | 'challenge_prompt'
  | 'challenge_input'
  | 'challenge_correct'
  | 'round_fail'
  | 'done'

const CHALLENGE_LABELS: Record<ChallengeMode, string> = {
  ascending: 'Ascending order (smallest to largest)',
  descending: 'Descending order (largest to smallest)',
  even_odd: 'Even numbers first, then odd numbers',
  odd_even: 'Odd numbers first, then even numbers',
  every_other: 'Every other digit (1st, 3rd, 5th…)',
}

function generateDigits(length: number): number[] {
  const digits: number[] = []
  for (let i = 0; i < length; i++) {
    digits.push(Math.floor(Math.random() * 10))
  }
  return digits
}

function pickChallenge(digits: number[]): ChallengeMode {
  const modes: ChallengeMode[] = ['ascending', 'descending', 'even_odd', 'odd_even', 'every_other']
  const hasEven = digits.some((d) => d % 2 === 0)
  const hasOdd = digits.some((d) => d % 2 !== 0)
  const available = modes.filter((m) => {
    if ((m === 'even_odd' || m === 'odd_even') && (!hasEven || !hasOdd)) return false
    return true
  })
  return available[Math.floor(Math.random() * available.length)]
}

function getExpectedChallenge(digits: number[], mode: ChallengeMode): number[] {
  switch (mode) {
    case 'ascending':
      return [...digits].sort((a, b) => a - b)
    case 'descending':
      return [...digits].sort((a, b) => b - a)
    case 'even_odd':
      return [...digits.filter((d) => d % 2 === 0), ...digits.filter((d) => d % 2 !== 0)]
    case 'odd_even':
      return [...digits.filter((d) => d % 2 !== 0), ...digits.filter((d) => d % 2 === 0)]
    case 'every_other':
      return digits.filter((_, i) => i % 2 === 0)
  }
}

/**
 * Parse user input into an array of single digits.
 * Accepts "1 1 8", "1,1,8", or "118" (contiguous single-digit numbers).
 * For multi-digit numbers in challenge modes (e.g. ascending "1 3 8"),
 * spaces/commas separate values; bare strings are split char-by-char.
 */
function parseDigitInput(raw: string): number[] {
  const trimmed = raw.trim()
  if (!trimmed) return []
  const parts = trimmed.split(/[\s,]+/).filter(Boolean)
  if (parts.length === 1 && parts[0].length > 1 && /^\d+$/.test(parts[0])) {
    return parts[0].split('').map(Number)
  }
  return parts.map(Number).filter((n) => !isNaN(n))
}

export function DigitSpanExercise({ exercise, onComplete }: DigitSpanExerciseProps) {
  const params = exercise.digitSpanParams
  const startLength = params?.startLength ?? 3
  const displayTimeMs = params?.displayTimeMs ?? 3000
  const maxLength = params?.maxLength ?? 15

  const [phase, setPhase] = useState<Phase>('intro')
  const [round, setRound] = useState<RoundState>({ digits: [], length: startLength, roundIndex: 0 })
  const [userInput, setUserInput] = useState<string>('')
  const [countdown, setCountdown] = useState(0)
  const [challenge, setChallenge] = useState<ChallengeMode | null>(null)
  const [feedback, setFeedback] = useState<string>('')
  const [maxReached, setMaxReached] = useState(0)
  const [totalRounds, setTotalRounds] = useState(0)
  const [totalChallenges, setTotalChallenges] = useState(0)
  const [challengesPassed, setChallengesPassed] = useState(0)
  const completedRef = useRef(false)
  const inputRef = useRef<HTMLInputElement>(null)
  const statsRef = useRef({ maxReached: 0, totalRounds: 0, totalChallenges: 0, challengesPassed: 0 })

  useEffect(() => {
    statsRef.current = { maxReached, totalRounds, totalChallenges, challengesPassed }
  }, [maxReached, totalRounds, totalChallenges, challengesPassed])

  const expectedForward = useMemo(() => round.digits, [round.digits])
  const expectedChallenge = useMemo(
    () => (challenge ? getExpectedChallenge(round.digits, challenge) : []),
    [round.digits, challenge]
  )

  const startRound = useCallback(
    (length: number, roundIdx: number) => {
      const digits = generateDigits(length)
      setRound({ digits, length, roundIndex: roundIdx })
      setUserInput('')
      setFeedback('')
      setChallenge(null)
      setPhase('showing')
      setCountdown(Math.ceil(displayTimeMs / 1000))
    },
    [displayTimeMs]
  )

  // countdown timer during 'showing' phase
  useEffect(() => {
    if (phase !== 'showing') return
    if (countdown <= 0) {
      setPhase('hiding')
      return
    }
    const t = setTimeout(() => setCountdown((c) => c - 1), 1000)
    return () => clearTimeout(t)
  }, [phase, countdown])

  // hiding animation: brief 600ms transition
  useEffect(() => {
    if (phase !== 'hiding') return
    const t = setTimeout(() => {
      setPhase('forward_input')
    }, 600)
    return () => clearTimeout(t)
  }, [phase])

  // auto-focus input when entering input phases
  useEffect(() => {
    if (phase === 'forward_input' || phase === 'challenge_input') {
      setTimeout(() => inputRef.current?.focus(), 50)
    }
  }, [phase])

  // brief pauses for correct/fail feedback
  useEffect(() => {
    if (phase === 'forward_correct') {
      const t = setTimeout(() => {
        const mode = pickChallenge(round.digits)
        setChallenge(mode)
        setUserInput('')
        setPhase('challenge_prompt')
      }, 1200)
      return () => clearTimeout(t)
    }
  }, [phase, round.digits])

  useEffect(() => {
    if (phase === 'challenge_prompt') {
      const t = setTimeout(() => {
        setPhase('challenge_input')
      }, 2000)
      return () => clearTimeout(t)
    }
  }, [phase])

  useEffect(() => {
    if (phase === 'challenge_correct') {
      const nextLength = round.length + 1
      if (nextLength > maxLength) {
        finishExercise()
        return
      }
      const t = setTimeout(() => {
        startRound(nextLength, round.roundIndex + 1)
      }, 1200)
      return () => clearTimeout(t)
    }
  }, [phase, round.length, round.roundIndex, maxLength, startRound])

  const finishExercise = useCallback(() => {
    if (completedRef.current) return
    completedRef.current = true
    setPhase('done')
    const s = statsRef.current
    const startL = startLength
    const rawScore = s.maxReached <= startL ? 0 : (s.maxReached - startL) / (maxLength - startL)
    const score = Math.max(0, Math.min(1, rawScore))

    onComplete?.({
      score,
      subscores: [
        { label: 'Max span', value: s.maxReached },
        { label: 'Rounds', value: s.totalRounds },
        { label: 'Challenges passed', value: `${s.challengesPassed}/${s.totalChallenges}` },
      ],
    })
  }, [startLength, maxLength, onComplete])

  const handleForwardSubmit = useCallback(() => {
    const entered = parseDigitInput(userInput)
    const correct =
      entered.length === expectedForward.length && entered.every((d, i) => d === expectedForward[i])

    setTotalRounds((r) => r + 1)

    if (correct) {
      setMaxReached((m) => Math.max(m, round.length))
      setFeedback('Correct! Now for the challenge...')
      setPhase('forward_correct')
    } else {
      setFeedback(`Wrong! Expected: ${expectedForward.join(' ')}`)
      setMaxReached((m) => Math.max(m, round.length - 1))
      setPhase('round_fail')
    }
  }, [userInput, expectedForward, round.length])

  const handleChallengeSubmit = useCallback(() => {
    const entered = parseDigitInput(userInput)
    const correct =
      entered.length === expectedChallenge.length &&
      entered.every((d, i) => d === expectedChallenge[i])

    setTotalChallenges((c) => c + 1)

    if (correct) {
      setChallengesPassed((c) => c + 1)
      setFeedback('Challenge passed!')
      setPhase('challenge_correct')
    } else {
      setFeedback(`Wrong! Expected: ${expectedChallenge.join(' ')}`)
      setPhase('round_fail')
    }
  }, [userInput, expectedChallenge])

  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent) => {
      if (e.key === 'Enter') {
        if (phase === 'forward_input') handleForwardSubmit()
        else if (phase === 'challenge_input') handleChallengeSubmit()
      }
    },
    [phase, handleForwardSubmit, handleChallengeSubmit]
  )

  const handleRoundFailContinue = useCallback(() => {
    finishExercise()
  }, [finishExercise])

  // ----- Render -----

  if (phase === 'intro') {
    return (
      <div className="digit-span-intro">
        <p className="prompt">{exercise.prompt}</p>
        <div className="digit-span-instructions">
          <p>A sequence of digits will flash on screen.</p>
          <p>Memorize them, then type them back in order.</p>
          <p>If correct, you'll get a challenge (sort, filter, etc.).</p>
          <p>The sequence grows with each success!</p>
        </div>
        <button type="button" className="digit-span-start-btn" onClick={() => startRound(startLength, 0)}>
          Start
        </button>
      </div>
    )
  }

  if (phase === 'showing') {
    return (
      <div className="digit-span-showing">
        <p className="digit-span-countdown">Remember these digits! ({countdown}s)</p>
        <div className={`digit-span-digits ${countdown <= 1 ? 'blink' : ''}`}>
          {round.digits.map((d, i) => (
            <span key={i} className="digit-span-digit">
              {d}
            </span>
          ))}
        </div>
      </div>
    )
  }

  if (phase === 'hiding') {
    return (
      <div className="digit-span-hiding">
        <div className="digit-span-digits fade-out">
          {round.digits.map((_, i) => (
            <span key={i} className="digit-span-digit hidden-digit">
              ?
            </span>
          ))}
        </div>
      </div>
    )
  }

  if (phase === 'forward_input') {
    return (
      <div className="digit-span-input">
        <p className="digit-span-prompt">Type the {round.length} digits in order:</p>
        <input
          ref={inputRef}
          type="text"
          inputMode="numeric"
          className="digit-span-input-field"
          value={userInput}
          onChange={(e) => setUserInput(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder={`e.g. ${round.digits.map(() => '0').join('')} or ${round.digits.map(() => '0').join(' ')}`}
          autoFocus
        />
        <button type="button" className="digit-span-submit-btn" onClick={handleForwardSubmit}>
          Submit
        </button>
      </div>
    )
  }

  if (phase === 'forward_correct') {
    return (
      <div className="digit-span-feedback correct">
        <p className="digit-span-feedback-text">{feedback}</p>
      </div>
    )
  }

  if (phase === 'challenge_prompt') {
    return (
      <div className="digit-span-challenge-prompt">
        <p className="digit-span-challenge-title">Challenge!</p>
        <p className="digit-span-challenge-desc">
          {challenge ? CHALLENGE_LABELS[challenge] : ''}
        </p>
        <p className="digit-span-challenge-digits">
          Original digits: <strong>{round.digits.join(' ')}</strong>
        </p>
      </div>
    )
  }

  if (phase === 'challenge_input') {
    return (
      <div className="digit-span-input">
        <p className="digit-span-prompt">
          {challenge ? CHALLENGE_LABELS[challenge] : 'Challenge'}
        </p>
        <p className="digit-span-challenge-reminder">
          Digits were: <strong>{round.digits.join(' ')}</strong>
        </p>
        <input
          ref={inputRef}
          type="text"
          inputMode="numeric"
          className="digit-span-input-field"
          value={userInput}
          onChange={(e) => setUserInput(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder={`${expectedChallenge.length} digits, e.g. ${expectedChallenge.map(() => '0').join('')}`}
          autoFocus
        />
        <button type="button" className="digit-span-submit-btn" onClick={handleChallengeSubmit}>
          Submit
        </button>
      </div>
    )
  }

  if (phase === 'challenge_correct') {
    return (
      <div className="digit-span-feedback correct">
        <p className="digit-span-feedback-text">{feedback}</p>
        <p className="digit-span-next-hint">Next: {round.length + 1} digits...</p>
      </div>
    )
  }

  if (phase === 'round_fail') {
    return (
      <div className="digit-span-feedback fail">
        <p className="digit-span-feedback-text">{feedback}</p>
        <p className="digit-span-max-span">Your max span: {Math.max(maxReached, round.length - 1)}</p>
        <button type="button" className="digit-span-end-btn" onClick={handleRoundFailContinue}>
          End Exercise
        </button>
      </div>
    )
  }

  if (phase === 'done') {
    return (
      <div className="digit-span-done">
        <p className="digit-span-result">Exercise complete!</p>
        <p className="digit-span-stats">
          Max span reached: {maxReached} digits
        </p>
      </div>
    )
  }

  return null
}
