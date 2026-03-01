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
const FR_ACCENTS = ['é', 'è', 'ê', 'à', 'â', 'ù', 'û', 'î', 'ô', 'ç', 'œ']

function getTileStates(guess: string, answer: string): TileState[] {
  const len = answer.length
  const states: TileState[] = Array(len).fill('absent')
  const remaining: (string | null)[] = answer.split('')

  for (let i = 0; i < len; i++) {
    if (guess[i] === answer[i]) {
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
    const states = getTileStates(g, answer)
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

      const won = guess === answer
      const lost = !won && newGuesses.length >= maxAttempts

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
    [guesses, answer, maxAttempts, wordLength, onComplete]
  )

  const handleLetter = useCallback(
    (char: string) => {
      if (phase !== 'playing') return
      if (currentLetters.length >= wordLength) return
      setCurrentLetters((prev) => [...prev, char.toLowerCase()])
    },
    [phase, currentLetters.length, wordLength]
  )

  const handleBackspace = useCallback(() => {
    if (phase !== 'playing') return
    setCurrentLetters((prev) => prev.slice(0, -1))
  }, [phase])

  const handleEnter = useCallback(() => {
    if (phase !== 'playing') return
    if (currentLetters.length !== wordLength) {
      setShakeRow(true)
      setTimeout(() => setShakeRow(false), 500)
      return
    }
    submitGuess(currentLetters)
  }, [phase, currentLetters, wordLength, submitGuess])

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.ctrlKey || e.altKey || e.metaKey) return
      if (e.key === 'Backspace') handleBackspace()
      else if (e.key === 'Enter') handleEnter()
      else if (e.key.length === 1 && /[a-zA-ZÀ-ÿœæ]/u.test(e.key)) handleLetter(e.key)
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [handleLetter, handleBackspace, handleEnter])

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
          const states = isSubmitted ? getTileStates(guesses[rowIdx], answer) : null
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
            {rowIdx === keyboardRows.length - 1 && (
              <button type="button" className="wordle-key wordle-key--action" onClick={handleEnter}>
                {isFrench ? 'Entrée' : 'Enter'}
              </button>
            )}
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
        {isFrench && (
          <div className="wordle-keyboard-row wordle-keyboard-row--accents">
            {FR_ACCENTS.map((c) => (
              <button
                key={c}
                type="button"
                className={`wordle-key wordle-key--letter ${letterHints[c] ?? ''}`}
                onClick={() => handleLetter(c)}
              >
                {c.toUpperCase()}
              </button>
            ))}
          </div>
        )}
      </div>

      <p className="wordle-hint-text">
        {isFrench
          ? `Essais restants : ${maxAttempts - guesses.length}`
          : `Attempts left: ${maxAttempts - guesses.length}`}
      </p>
    </div>
  )
}
