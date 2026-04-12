import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import type { ExerciseDto } from '../../types/api'
import type { ExerciseResult } from '../../types/exercise'

export interface DigitSpanExerciseProps {
  exercise: ExerciseDto
  onComplete?: (result: ExerciseResult | number) => void
}

type DigitSpanTask =
  | 'FORWARD_ORDER'
  | 'ASCENDING'
  | 'DESCENDING'
  | 'EVEN_THEN_ODD'
  | 'ODD_THEN_EVEN'
  | 'EVERY_OTHER_FROM_FIRST'
  | 'EVERY_OTHER_FROM_SECOND'

function parseTasks(raw: string[] | undefined): DigitSpanTask[] {
  if (!raw?.length) return []
  const allowed = new Set<string>([
    'FORWARD_ORDER',
    'ASCENDING',
    'DESCENDING',
    'EVEN_THEN_ODD',
    'ODD_THEN_EVEN',
    'EVERY_OTHER_FROM_FIRST',
    'EVERY_OTHER_FROM_SECOND',
  ])
  return raw.filter((t): t is DigitSpanTask => allowed.has(t))
}

function expectedForTask(seq: number[], task: DigitSpanTask): number[] {
  switch (task) {
    case 'FORWARD_ORDER':
      return [...seq]
    case 'ASCENDING':
      return [...seq].sort((a, b) => a - b)
    case 'DESCENDING':
      return [...seq].sort((a, b) => b - a)
    case 'EVEN_THEN_ODD': {
      const evens = seq.filter((d) => d % 2 === 0).sort((a, b) => a - b)
      const odds = seq.filter((d) => d % 2 !== 0).sort((a, b) => a - b)
      return [...evens, ...odds]
    }
    case 'ODD_THEN_EVEN': {
      const odds = seq.filter((d) => d % 2 !== 0).sort((a, b) => a - b)
      const evens = seq.filter((d) => d % 2 === 0).sort((a, b) => a - b)
      return [...odds, ...evens]
    }
    case 'EVERY_OTHER_FROM_FIRST':
      return seq.filter((_, i) => i % 2 === 0)
    case 'EVERY_OTHER_FROM_SECOND':
      return seq.filter((_, i) => i % 2 === 1)
    default:
      return [...seq]
  }
}

function taskLabelFr(task: DigitSpanTask): string {
  switch (task) {
    case 'FORWARD_ORDER':
      return "Dans l'ordre d'affichage"
    case 'ASCENDING':
      return 'Ordre croissant'
    case 'DESCENDING':
      return 'Ordre décroissant'
    case 'EVEN_THEN_ODD':
      return 'Pairs puis impairs (croissant dans chaque groupe)'
    case 'ODD_THEN_EVEN':
      return 'Impairs puis pairs (croissant dans chaque groupe)'
    case 'EVERY_OTHER_FROM_FIRST':
      return 'Un sur deux en commençant par le premier'
    case 'EVERY_OTHER_FROM_SECOND':
      return 'Un sur deux en commençant par le second'
    default:
      return task
  }
}

/** Stable seed from UUID string for local RNG */
function hashSeed(s: string): number {
  let h = 0
  for (let i = 0; i < s.length; i++) h = (Math.imul(31, h) + s.charCodeAt(i)) | 0
  return Math.abs(h)
}

function mulberry32(a: number) {
  return function () {
    let t = (a += 0x6d2b79f5)
    t = Math.imul(t ^ (t >>> 15), t | 1)
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61)
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296
  }
}

function randomDigit(rng: () => number, minD: number, maxD: number): number {
  return minD + Math.floor(rng() * (maxD - minD + 1))
}

export function DigitSpanExercise({ exercise, onComplete }: DigitSpanExerciseProps) {
  const p = exercise.digitSpanParams
  const tasks = useMemo(() => parseTasks(p?.tasks), [p?.tasks])

  const completedRef = useRef(false)
  const displaySeconds = p?.displaySeconds ?? 3
  const progressive = p?.progressive ?? false
  const maxLen = p?.maxLength ?? 12
  const minD = p?.minDigit ?? 0
  const maxD = p?.maxDigit ?? 9

  const [round, setRound] = useState(0)
  const [sequence, setSequence] = useState<number[]>(() => p?.sequence ?? [])
  const [phase, setPhase] = useState<'memorize' | 'hide' | 'recall' | 'between' | 'done'>('memorize')
  const [countdown, setCountdown] = useState(displaySeconds)
  const [taskIndex, setTaskIndex] = useState(0)
  const [input, setInput] = useState('')
  const [feedback, setFeedback] = useState<'ok' | 'bad' | null>(null)
  /** Longest span for which all tasks were completed (progressive mode). */
  const [longestCompleted, setLongestCompleted] = useState(0)
  const [gameOver, setGameOver] = useState(false)

  const rngRef = useRef<() => number>()
  if (!rngRef.current) {
    rngRef.current = mulberry32(hashSeed(exercise.id + '-digit-span'))
  }
  const rng = rngRef.current

  useEffect(() => {
    completedRef.current = false
  }, [exercise.id])

  useEffect(() => {
    if (!p?.sequence?.length || tasks.length === 0) return
    setSequence(p.sequence)
    setPhase('memorize')
    setCountdown(displaySeconds)
    setTaskIndex(0)
    setInput('')
    setFeedback(null)
    setGameOver(false)
    setRound(0)
    setLongestCompleted(0)
  }, [exercise.id, p?.sequence, displaySeconds, tasks.length])

  const currentExpected = useMemo(() => {
    if (tasks.length === 0 || taskIndex >= tasks.length) return []
    return expectedForTask(sequence, tasks[taskIndex]!)
  }, [sequence, tasks, taskIndex])

  // Memorize countdown
  useEffect(() => {
    if (phase !== 'memorize' || gameOver) return
    if (countdown <= 0) {
      const t = setTimeout(() => setPhase('hide'), 0)
      return () => clearTimeout(t)
    }
    const id = setInterval(() => setCountdown((c) => c - 1), 1000)
    return () => clearInterval(id)
  }, [phase, countdown, gameOver])

  useEffect(() => {
    if (phase !== 'hide') return
    const t = setTimeout(() => {
      setPhase('recall')
      setInput('')
      setFeedback(null)
    }, 400)
    return () => clearTimeout(t)
  }, [phase])

  const finishWithScore = useCallback(
    (score: number, sub?: { label: string; value: string }) => {
      if (completedRef.current) return
      completedRef.current = true
      onComplete?.({
        score,
        subscores: sub ? [sub] : undefined,
      })
    },
    [onComplete]
  )

  const startNextProgressiveRound = useCallback(
    (completedLen: number, prevSeq: number[]) => {
      setLongestCompleted((c) => Math.max(c, completedLen))
      const nextLen = prevSeq.length + 1
      if (nextLen > maxLen) {
        setPhase('done')
        finishWithScore(1, { label: 'Série', value: `${completedLen} chiffres (maximum atteint)` })
        return
      }
      const extra = randomDigit(rng, minD, maxD)
      const next = [...prevSeq, extra]
      setSequence(next)
      setTaskIndex(0)
      setInput('')
      setFeedback(null)
      setCountdown(displaySeconds)
      setPhase('memorize')
      setRound((r) => r + 1)
    },
    [displaySeconds, finishWithScore, maxLen, minD, maxD, rng]
  )

  const handleSubmitRecall = useCallback(() => {
    if (phase !== 'recall' || gameOver) return
    const digits = input.replace(/\D/g, '').split('').map(Number)
    const ok =
      digits.length === currentExpected.length && digits.every((d, i) => d === currentExpected[i])

    if (!ok) {
      setFeedback('bad')
      setGameOver(true)
      if (progressive) {
        const score =
          maxLen > 0 ? Math.min(1, Math.max(0, longestCompleted / maxLen)) : 0
        setPhase('done')
        finishWithScore(score, {
          label: 'Meilleure série',
          value: `${longestCompleted} chiffre${longestCompleted > 1 ? 's' : ''}`,
        })
      } else {
        setPhase('done')
        finishWithScore(0)
      }
      return
    }

    setFeedback('ok')
    setTimeout(() => {
      setFeedback(null)
      setInput('')
      if (taskIndex + 1 < tasks.length) {
        setTaskIndex((i) => i + 1)
      } else {
        if (progressive) {
          startNextProgressiveRound(sequence.length, sequence)
        } else {
          setPhase('done')
          finishWithScore(1, { label: 'Tâches', value: `${tasks.length}/${tasks.length}` })
        }
      }
    }, 350)
  }, [
    phase,
    gameOver,
    input,
    currentExpected,
    taskIndex,
    tasks.length,
    progressive,
    sequence,
    startNextProgressiveRound,
    finishWithScore,
    longestCompleted,
    maxLen,
  ])

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (phase !== 'recall' || gameOver) return
      if (e.key === 'Enter') {
        e.preventDefault()
        handleSubmitRecall()
      }
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [phase, gameOver, handleSubmitRecall])

  if (!p || !p.sequence?.length || tasks.length === 0) {
    return <p className="error">Exercice invalide : paramètres digit span manquants.</p>
  }

  return (
    <div className="digit-span-exercise">
      <p className="prompt">{exercise.prompt}</p>

      {phase === 'memorize' && (
        <div className="digit-span-memorize">
          <div className="digit-span-digits" aria-live="polite">
            {sequence.map((d, i) => (
              <span key={`${round}-${i}`} className="digit-span-digit pulse-soft">
                {d}
              </span>
            ))}
          </div>
          <p className="digit-span-countdown">
            Mémorisez… <strong>{countdown}</strong>s
          </p>
        </div>
      )}

      {phase === 'hide' && <div className="digit-span-hide-panel" />}

      {phase === 'recall' && !gameOver && (
        <div className="digit-span-recall enter-in">
          <p className="digit-span-task-hint">{taskLabelFr(tasks[taskIndex]!)}</p>
          <p className="digit-span-task-meta">
            Saisissez {currentExpected.length} chiffre{currentExpected.length > 1 ? 's' : ''} (sans espaces).
          </p>
          <div className="input-row">
            <input
              type="text"
              inputMode="numeric"
              autoComplete="off"
              value={input}
              onChange={(e) => setInput(e.target.value.replace(/\D/g, ''))}
              placeholder="012…"
              autoFocus
              className={feedback === 'ok' ? 'correct' : undefined}
            />
            <button type="button" onClick={handleSubmitRecall}>
              Valider
            </button>
          </div>
          {feedback === 'ok' && <span className="correct">✓</span>}
        </div>
      )}

      {gameOver && phase === 'done' && (
        <p className="incorrect" role="status">
          Série terminée.
        </p>
      )}
    </div>
  )
}
