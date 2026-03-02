import { useState, useCallback, useEffect } from 'react'
import type { ExerciseDto } from '../../types/api'
import type { ExerciseResult } from '../../types/exercise'

export interface WordleExerciseProps {
  exercise: ExerciseDto
  onComplete?: (result: ExerciseResult | number) => void
}

type TileState = 'correct' | 'present' | 'absent' | 'empty' | 'active'
type LetterHint = 'correct' | 'present' | 'absent' | undefined

const KEYBOARD_ROWS_QWERTY = [
  ['q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p'],
  ['a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l'],
  ['z', 'x', 'c', 'v', 'b', 'n', 'm'],
]
const KEYBOARD_ROWS_AZERTY = [
  ['a', 'z', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p'],
  ['q', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'm'],
  ['w', 'x', 'c', 'v', 'b', 'n'],
]

/** Normalize for comparison: é/è/ê → e, à/â → a, etc. so "e" matches "é". */
export function normalizeForCompare(str: string): string {
  const nfd = str
    .toLowerCase()
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
  return nfd.replace(/\u0153/g, 'oe').replace(/\u00e6/g, 'ae')
}

export function getTileStates(guess: string, answerNormalized: string): TileState[] {
  const len = answerNormalized.length
  const states: TileState[] = Array(len).fill('absent')
  const remaining: (string | null)[] = answerNormalized.split('')

  for (let i = 0; i < len; i++) {
    if (guess[i] === answerNormalized[i]) {
      states[i] = 'correct'
      remaining[i] = null
    }
  }
  for (let i = 0; i < len; i++) {
    if (states[i] === 'correct') continue
    const idx = remaining.indexOf(guess[i])
    if (idx >= 0) {
      states[i] = 'present'
      remaining[idx] = null
    }
  }
  return states
}

/**
 * Classic Wordle: guess a word of N letters in up to maxAttempts tries.
 * Green = correct position, yellow = present but wrong position, gray = absent.
 * Supports French (AZERTY + accents) and English (QWERTY) keyboards.
 */
export function WordleExercise({ exercise, onComplete }: WordleExerciseProps) {
  const params = exercise.wordleParams
  if (!params?.answer) {
    return <p className="error">Invalid wordle exercise: missing answer.</p>
  }

  const answer = params.answer.toLowerCase()
  const answerNormalized = normalizeForCompare(answer)
  const wordLength = params.wordLength
  const maxAttempts = params.maxAttempts ?? 6
  const language = params.language ?? 'fr'
  const isFrench = language === 'fr'

  const [guesses, setGuesses] = useState<string[]>([])
  const [currentLetters, setCurrentLetters] = useState<string[]>([])
  const [phase, setPhase] = useState<'playing' | 'won' | 'lost'>('playing')
  const [shakeRow, setShakeRow] = useState(false)
  const [revealRow, setRevealRow] = useState<number | null>(null)

  const letterHints: Record<string, LetterHint> = {}
  for (const g of guesses) {
    const states = getTileStates(g, answerNormalized)
    for (let i = 0; i < g.length; i++) {
      const c = g[i]
      const s = states[i] as LetterHint
      const existing = letterHints[c]
      if (!existing || (existing !== 'correct' && s === 'correct') || (existing === 'absent' && s === 'present')) {
        letterHints[c] = s
      }
    }
  }

  const submitGuess = useCallback(
    (letters: string[]) => {
      const guess = letters.join('')
      const newGuesses = [...guesses, guess]
      const rowIdx = guesses.length
      setGuesses(newGuesses)
      setCurrentLetters([])
      setRevealRow(rowIdx)

      const won = guess === answerNormalized
      const lost = !won && newGuesses.length >= maxAttempts
      if (!won) {
        setShakeRow(true)
        setTimeout(() => setShakeRow(false), 500)
      }

      setTimeout(() => {
        setRevealRow(null)
        if (won) {
          setPhase('won')
          const score = Math.max(0.5, 1 - guesses.length * 0.1)
          onComplete?.({
            score,
            subscores: [
              { label: 'Attempts', value: newGuesses.length },
              { label: 'Letters', value: wordLength },
            ],
          })
        } else if (lost) {
          setPhase('lost')
          onComplete?.({ score: 0.1 })
        }
      }, wordLength * 350 + 200)
    },
    [guesses, answerNormalized, maxAttempts, wordLength, onComplete]
  )

  const handleLetter = useCallback(
    (char: string) => {
      if (phase !== 'playing') return
      if (currentLetters.length >= wordLength) return
      const normalized = normalizeForCompare(char)
      if (!normalized) return
      const next = [...currentLetters, normalized]
      setCurrentLetters(next)
      if (next.length === wordLength) {
        submitGuess(next)
      }
    },
    [phase, currentLetters, wordLength, submitGuess]
  )

  const handleBackspace = useCallback(() => {
    if (phase !== 'playing') return
    setCurrentLetters((prev) => prev.slice(0, -1))
  }, [phase])

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.ctrlKey || e.altKey || e.metaKey) return
      if (e.key === 'Backspace') handleBackspace()
      else if (e.key === 'Enter' && currentLetters.length === wordLength) submitGuess(currentLetters)
      else if (e.key.length === 1 && /[a-zA-ZÀ-ÿœæ]/u.test(e.key)) handleLetter(e.key)
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [handleLetter, handleBackspace, submitGuess, currentLetters, wordLength])

  const keyboardRows = isFrench ? KEYBOARD_ROWS_AZERTY : KEYBOARD_ROWS_QWERTY

  return (
    <div className="wordle">
      <p className="wordle-prompt">{exercise.prompt}</p>

      {/* Grid */}
      <div className="wordle-grid" style={{ '--word-length': wordLength } as React.CSSProperties}>
        {Array.from({ length: maxAttempts }, (_, rowIdx) => {
          const isSubmitted = rowIdx < guesses.length
          const isCurrent = rowIdx === guesses.length && phase === 'playing'
          const guess = isSubmitted ? guesses[rowIdx] : isCurrent ? currentLetters.join('') : ''
          const states = isSubmitted ? getTileStates(guesses[rowIdx], answerNormalized) : null
          const isRevealing = revealRow === rowIdx

          return (
            <div
              key={rowIdx}
              className={`wordle-row ${isCurrent && shakeRow ? 'shake' : ''}`}
            >
              {Array.from({ length: wordLength }, (_, colIdx) => {
                const char = guess[colIdx] ?? ''
                let tileState: TileState = 'empty'
                if (isSubmitted && states) {
                  tileState = states[colIdx] as TileState
                } else if (isCurrent && char) {
                  tileState = 'active'
                }

                return (
                  <div
                    key={colIdx}
                    className={`wordle-tile ${tileState}`}
                    style={isRevealing ? { animationDelay: `${colIdx * 350}ms` } : undefined}
                    data-revealing={isRevealing ? 'true' : undefined}
                  >
                    {char.toUpperCase()}
                  </div>
                )
              })}
            </div>
          )
        })}
      </div>

      {/* Status */}
      {phase === 'won' && (
        <p className="wordle-status wordle-status--won">
          {isFrench
            ? `Bravo ! Trouvé en ${guesses.length} essai${guesses.length > 1 ? 's' : ''} !`
            : `Well done! Found in ${guesses.length} attempt${guesses.length > 1 ? 's' : ''}!`}
        </p>
      )}
      {phase === 'lost' && (
        <p className="wordle-status wordle-status--lost">
          {isFrench ? `Le mot était : ` : `The word was: `}
          <strong>{answer.toUpperCase()}</strong>
        </p>
      )}

      {/* On-screen keyboard */}
      <div className="wordle-keyboard">
        {keyboardRows.map((row, rowIdx) => (
          <div key={rowIdx} className="wordle-keyboard-row">
            {row.map((key) => (
              <button
                key={key}
                type="button"
                className={`wordle-key wordle-key--letter ${letterHints[key] ?? ''}`}
                onClick={() => handleLetter(key)}
              >
                {key.toUpperCase()}
              </button>
            ))}
            {rowIdx === keyboardRows.length - 1 && (
              <button type="button" className="wordle-key wordle-key--action" onClick={handleBackspace}>
                ⌫
              </button>
            )}
          </div>
        ))}
      </div>

      <p className="wordle-hint-text">
        {isFrench
          ? `Essais restants : ${maxAttempts - guesses.length}`
          : `Attempts left: ${maxAttempts - guesses.length}`}
      </p>
    </div>
  )
}
