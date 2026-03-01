import { useState, useCallback, useEffect, useRef } from 'react'
import type { ExerciseDto } from '../../types/api'
import type { ExerciseResult } from '../../types/exercise'

export interface AnagramExerciseProps {
  exercise: ExerciseDto
  onComplete?: (result: ExerciseResult | number) => void
}

/**
 * Anagram game: scrambled letters, find the word.
 * Wordle-style: grid of slots, keyboard with only anagram letters.
 * Hint every N seconds (from params); each letter/backspace restarts the timer.
 * letterColorHint: green/red per slot (another kind of hint when enabled).
 */
export function AnagramExercise({ exercise, onComplete }: AnagramExerciseProps) {
  const params = exercise.anagramParams
  if (!params?.scrambledLetters?.length || !params.answer) {
    return <p className="error">Invalid anagram exercise: missing letters or answer.</p>
  }

  const answer = params.answer.toLowerCase()
  const letters = params.scrambledLetters.map((c) => c.toLowerCase())
  const len = answer.length
  const hintIntervalSeconds = params.hintIntervalSeconds ?? 10
  const hintIntervalMs = hintIntervalSeconds * 1000
  const letterColorHint = params.letterColorHint ?? true

  const [slots, setSlots] = useState<string[]>(() => Array(len).fill(''))
  const [hintIndex, setHintIndex] = useState(0)
  const [wrongPlacements, setWrongPlacements] = useState(0)
  const [phase, setPhase] = useState<'intro' | 'playing' | 'done'>('intro')
  const [hintResetKey, setHintResetKey] = useState(0)
  const startRef = useRef<number | null>(null)

  const currentInput = slots.join('').toLowerCase()
  const isCorrect = currentInput === answer

  const restartHintTimer = useCallback(() => setHintResetKey((k) => k + 1), [])

  // Hint after N seconds of inactivity; typing/backspace restarts the timer
  useEffect(() => {
    if (phase !== 'playing') return
    const id = setTimeout(() => {
      setHintIndex((prev) => {
        if (prev >= len) return prev
        const next = prev + 1
        setSlots((s) => {
          const nextSlots = [...s]
          nextSlots[prev] = answer[prev]
          return nextSlots
        })
        return next
      })
    }, hintIntervalMs)
    return () => clearTimeout(id)
  }, [phase, len, answer, hintResetKey, hintIntervalMs])

  // Success when all slots match answer
  useEffect(() => {
    if (phase !== 'playing' || !isCorrect) return
    setPhase('done')
    const hintsUsed = hintIndex
    const penalty = hintsUsed * 0.15 + wrongPlacements * 0.08
    const score = Math.max(0.3, 1 - penalty)
    onComplete?.({
      score,
      subscores: [
        { label: 'Hints', value: hintsUsed },
        { label: 'Wrong tries', value: wrongPlacements },
        { label: 'Letters', value: len },
      ],
    })
  }, [phase, isCorrect, hintIndex, wrongPlacements, len, onComplete])

  const letterCounts = letters.reduce<Record<string, number>>((acc, c) => {
    acc[c] = (acc[c] ?? 0) + 1
    return acc
  }, {})
  const usedLetterCounts = slots.reduce<Record<string, number>>((acc, c) => {
    if (c) acc[c] = (acc[c] ?? 0) + 1
    return acc
  }, {})

  const canAdd = (char: string) => {
    const used = usedLetterCounts[char] ?? 0
    const max = letterCounts[char] ?? 0
    return used < max
  }

  const handleLetter = useCallback(
    (char: string) => {
      if (phase !== 'playing') return
      if (!canAdd(char)) return
      restartHintTimer()
      const idx = slots.findIndex((s) => !s)
      if (idx < 0) return
      if (char !== answer[idx]) {
        setWrongPlacements((n) => n + 1)
      }
      setSlots((prev) => {
        const next = [...prev]
        next[idx] = char
        return next
      })
    },
    [phase, slots, canAdd, restartHintTimer, answer]
  )

  const handleBackspace = useCallback(() => {
    if (phase !== 'playing') return
    restartHintTimer()
    const idx = slots.map((s, i) => (s ? i : -1)).filter((i) => i >= 0).pop()
    if (idx == null) return
    setSlots((prev) => {
      const next = [...prev]
      next[idx] = ''
      return next
    })
  }, [phase, slots, restartHintTimer])

  const startGame = useCallback(() => {
    setSlots(Array(len).fill(''))
    setHintIndex(0)
    setWrongPlacements(0)
    setPhase('playing')
    startRef.current = Date.now()
  }, [len])

  if (phase === 'intro') {
    return (
      <div className="anagram-intro">
        <p className="prompt">{exercise.prompt}</p>
        <p className="anagram-instruction">
          Recomposez le mot à partir des lettres mélangées.
          {hintIntervalSeconds > 0
            ? ` Un indice apparaît après ${hintIntervalSeconds} secondes sans saisie.`
            : ' Aucun indice automatique.'}
          {letterColorHint && ' Les lettres vertes/rouges indiquent correct/incorrect.'}
        </p>
        <button type="button" onClick={startGame} className="anagram-start-btn">
          Commencer
        </button>
      </div>
    )
  }

  if (phase === 'done') {
    return (
      <div className="anagram-done">
        <p className="anagram-result">Mot trouvé !</p>
        <p className="anagram-answer">{answer}</p>
      </div>
    )
  }

  return (
    <div className="anagram-playing">
      {/* Scrambled letters hint */}
      <p className="anagram-letters-hint">
        Lettres : {letters.join(' ')}
      </p>
      {/* Word slots (Wordle-style grid) */}
      <div className="anagram-grid">
        {slots.map((char, i) => (
          <div
            key={i}
            className={`anagram-slot ${char ? 'filled' : 'empty'}`}
            data-correct={letterColorHint && char && char === answer[i] ? 'true' : undefined}
            data-incorrect={letterColorHint && char && char !== answer[i] ? 'true' : undefined}
          >
            {char.toUpperCase()}
          </div>
        ))}
      </div>
      {/* Keyboard: only anagram letters (each distinct letter shown once) */}
      <div className="anagram-keyboard">
        {letters
          .filter((c, i) => letters.indexOf(c) === i)
          .map((c) => (
          <button
            key={c}
            type="button"
            className="anagram-key"
            onClick={() => handleLetter(c)}
            disabled={!canAdd(c)}
          >
            {c.toUpperCase()}
          </button>
        ))}
        <button
          type="button"
          className="anagram-key anagram-key-back"
          onClick={handleBackspace}
        >
          ⌫
        </button>
      </div>
      <p className="anagram-hint-timer">
        {hintIntervalSeconds > 0
          ? `Indice après ${hintIntervalSeconds} secondes sans saisie`
          : 'Pas d\'indice automatique'}
      </p>
    </div>
  )
}
