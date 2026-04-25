import { useState, useCallback, useEffect, useMemo } from 'react'
import type { ExerciseDto } from '../../types/api'
import type { ExerciseResult } from '../../types/exercise'
import { loadWordListWords } from '../../utils/wordleWordLoader'
import { computeWordleComplexity } from '../../utils/wordleComplexity'

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

/**
 * Canonical form for answers, guesses, and word lists — matches Kotlin WordleGenerator.normalizeWordleWord
 * (NFD → strip combining marks → lowercase → œ→oe æ→ae).
 */
export function normalizeForCompare(str: string): string {
  const nfd = str.normalize('NFD')
  const noMarks = nfd.replace(/\p{M}+/gu, '')
  return noMarks.toLowerCase().replace(/\u0153/g, 'oe').replace(/\u00e6/g, 'ae')
}

function buildValidWordSet(words: string[], answer: string): Set<string> {
  const set = new Set<string>()
  for (const w of words) set.add(normalizeForCompare(w))
  set.add(normalizeForCompare(answer))
  return set
}

export function getTileStates(guess: string, answer: string): TileState[] {
  const g = normalizeForCompare(guess)
  const a = normalizeForCompare(answer)
  const len = a.length
  const states: TileState[] = Array(len).fill('absent')
  const remaining: (string | null)[] = a.split('')

  for (let i = 0; i < len; i++) {
    if (g[i] === a[i]) {
      states[i] = 'correct'
      remaining[i] = null
    }
  }
  for (let i = 0; i < len; i++) {
    if (states[i] === 'correct') continue
    const idx = remaining.indexOf(g[i])
    if (idx >= 0) {
      states[i] = 'present'
      remaining[idx] = null
    }
  }
  return states
}

function isAllowedGuess(candidate: string, answer: string, validWordSet: Set<string> | null): boolean {
  if (!validWordSet) return false
  if (validWordSet.has(candidate)) return true
  return normalizeForCompare(candidate) === normalizeForCompare(answer)
}

function formatTimeBudget(seconds: number): string {
  if (seconds < 60) return `${seconds}s`
  const m = Math.round(seconds / 60)
  return m <= 1 ? `1 min` : `${m} min`
}

/**
 * Classic Wordle: guess a word of N letters in up to maxAttempts tries.
 * Green = correct position, yellow = present but wrong position, gray = absent.
 * Supports French (AZERTY) and English (QWERTY) keyboards; words are unaccented.
 */
export function WordleExercise({ exercise, onComplete }: WordleExerciseProps) {
  const params = exercise.wordleParams
  if (!params?.answer) {
    return <p className="error">Invalid wordle exercise: missing answer.</p>
  }

  const answer = normalizeForCompare(params.answer)
  const wordLength = params.wordLength
  const maxAttempts = params.maxAttempts ?? 6
  const language = params.language ?? 'fr'
  const isFrench = language === 'fr'

  const wordleComplexity = useMemo(() => {
    return (
      exercise.wordleComplexity ??
      computeWordleComplexity({
        wordLength,
        maxAttempts,
        timeLimitSeconds: exercise.timeLimitSeconds,
        language,
      })
    )
  }, [exercise.wordleComplexity, exercise.timeLimitSeconds, wordLength, maxAttempts, language])

  const complexityTitle = isFrench
    ? `Indicateurs : entropie ≈ ${wordleComplexity.entropyBits.toFixed(1)} bits, espace log₁₀ ≈ ${wordleComplexity.searchSpaceLog10.toFixed(2)}, ${wordleComplexity.guessesPerLetter.toFixed(2)} essais par lettre, ${wordleComplexity.secondsPerGuessBudget.toFixed(0)} s par essai (budget).`
    : `Indicators: entropy ≈ ${wordleComplexity.entropyBits.toFixed(1)} bits, search space log₁₀ ≈ ${wordleComplexity.searchSpaceLog10.toFixed(2)}, ${wordleComplexity.guessesPerLetter.toFixed(2)} guesses per letter, ${wordleComplexity.secondsPerGuessBudget.toFixed(0)} s per guess (budget).`

  const complexityLine = isFrench
    ? `Difficulté ${wordleComplexity.difficultyScore0To100}/100 · ${wordLength} lettres · ${maxAttempts} essais · ${formatTimeBudget(exercise.timeLimitSeconds)}`
    : `Difficulty ${wordleComplexity.difficultyScore0To100}/100 · ${wordLength} letters · ${maxAttempts} tries · ${formatTimeBudget(exercise.timeLimitSeconds)}`

  const [guesses, setGuesses] = useState<string[]>([])
  const [currentLetters, setCurrentLetters] = useState<string[]>([])
  const [phase, setPhase] = useState<'playing' | 'won' | 'lost'>('playing')
  const [rowShake, setRowShake] = useState<'none' | 'incomplete' | 'invalid'>('none')
  const [revealRow, setRevealRow] = useState<number | null>(null)
  const [validWordSet, setValidWordSet] = useState<Set<string> | null>(null)
  const [dictStatus, setDictStatus] = useState<'loading' | 'ready' | 'error'>('loading')

  useEffect(() => {
    let cancelled = false
    setDictStatus('loading')
    setValidWordSet(null)
    loadWordListWords(language, wordLength)
      .then((words) => {
        if (cancelled) return
        setValidWordSet(buildValidWordSet(words, answer))
        setDictStatus('ready')
      })
      .catch(() => {
        if (cancelled) return
        setDictStatus('error')
      })
    return () => {
      cancelled = true
    }
  }, [language, wordLength, answer])

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
      const guess = normalizeForCompare(letters.join(''))
      const newGuesses = [...guesses, guess]
      const rowIdx = guesses.length
      setGuesses(newGuesses)
      setCurrentLetters([])
      setRevealRow(rowIdx)

      const won = normalizeForCompare(guess) === normalizeForCompare(answer)
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
      if (phase !== 'playing' || dictStatus !== 'ready') return
      if (currentLetters.length >= wordLength) return
      setCurrentLetters((prev) => [...prev, char.toLowerCase()])
    },
    [phase, dictStatus, currentLetters.length, wordLength]
  )

  const handleBackspace = useCallback(() => {
    if (phase !== 'playing' || dictStatus !== 'ready') return
    setCurrentLetters((prev) => prev.slice(0, -1))
  }, [phase, dictStatus])

  const handleEnter = useCallback(() => {
    if (phase !== 'playing') return
    if (dictStatus !== 'ready' || !validWordSet) return
    if (currentLetters.length !== wordLength) {
      setRowShake('incomplete')
      setTimeout(() => setRowShake('none'), 500)
      return
    }
    const candidate = normalizeForCompare(currentLetters.join(''))
    if (!isAllowedGuess(candidate, answer, validWordSet)) {
      setRowShake('invalid')
      setTimeout(() => setRowShake('none'), 550)
      return
    }
    submitGuess(currentLetters)
  }, [phase, dictStatus, currentLetters, wordLength, submitGuess, validWordSet, answer])

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.ctrlKey || e.altKey || e.metaKey) return
      if (dictStatus !== 'ready') return
      if (e.key === 'Backspace') handleBackspace()
      else if (e.key === 'Enter') handleEnter()
      else if (e.key.length === 1 && /[a-zA-Z]/u.test(e.key)) handleLetter(e.key)
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [handleLetter, handleBackspace, handleEnter, dictStatus])

  const keyboardRows = isFrench ? KEYBOARD_ROWS_AZERTY : KEYBOARD_ROWS_QWERTY

  const inputLocked = phase !== 'playing' || dictStatus !== 'ready'

  return (
    <div className="wordle" aria-busy={dictStatus === 'loading'}>
      <p className="wordle-prompt">{exercise.prompt}</p>
      <p className="wordle-complexity" title={complexityTitle} aria-label={complexityTitle}>
        {complexityLine}
      </p>
      {dictStatus === 'error' && (
        <p className="wordle-status wordle-status--lost" role="alert">
          {isFrench
            ? 'Impossible de charger le dictionnaire. Rechargez la page.'
            : 'Could not load word list. Refresh the page.'}
        </p>
      )}

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
              className={`wordle-row${isCurrent && rowShake !== 'none' ? ` wordle-row--${rowShake}` : ''}`}
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
              <button
                type="button"
                className="wordle-key wordle-key--action"
                onClick={handleEnter}
                disabled={inputLocked}
              >
                {isFrench ? 'Entrée' : 'Enter'}
              </button>
            )}
            {row.map((key) => (
              <button
                key={key}
                type="button"
                className={`wordle-key wordle-key--letter ${letterHints[key] ?? ''}`}
                onClick={() => handleLetter(key)}
                disabled={inputLocked}
              >
                {key.toUpperCase()}
              </button>
            ))}
            {rowIdx === keyboardRows.length - 1 && (
              <button
                type="button"
                className="wordle-key wordle-key--action"
                onClick={handleBackspace}
                disabled={inputLocked}
              >
                ⌫
              </button>
            )}
          </div>
        ))}
      </div>

      <p className="wordle-hint-text">
        {dictStatus === 'loading'
          ? isFrench
            ? 'Chargement du dictionnaire…'
            : 'Loading dictionary…'
          : dictStatus === 'ready'
            ? isFrench
              ? `Essais restants : ${maxAttempts - guesses.length}`
              : `Attempts left: ${maxAttempts - guesses.length}`
            : ''}
      </p>
    </div>
  )
}
