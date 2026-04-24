import { useState, useCallback, useEffect } from 'react'
import type { ExerciseDto } from '../../types/api'
import type { ExerciseResult } from '../../types/exercise'

export interface AnagramExerciseProps {
  exercise: ExerciseDto
  onComplete?: (result: ExerciseResult | number) => void
  showInstruction?: boolean
}

/**
 * Anagram game: scrambled letters, find the word.
 * Wordle-style: grid of slots, keyboard with only anagram letters.
 */
export function AnagramExercise({ exercise, onComplete, showInstruction = true }: AnagramExerciseProps) {
  const params = exercise.anagramParams
  if (!params?.scrambledLetters?.length || !params.answer) {
    return <p className="error">Invalid anagram exercise: missing letters or answer.</p>
  }

  const answer = params.answer.toLowerCase()
  const letters = params.scrambledLetters.map((c) => c.toLowerCase())
  const len = answer.length

  const [slots, setSlots] = useState<string[]>(() => Array(len).fill(''))
  const [wrongPlacements, setWrongPlacements] = useState(0)
  const [phase, setPhase] = useState<'intro' | 'playing' | 'done'>('intro')

  const currentInput = slots.join('').toLowerCase()
  const isCorrect = currentInput === answer

  const letterCounts = letters.reduce<Record<string, number>>((acc, c) => {
    acc[c] = (acc[c] ?? 0) + 1
    return acc
  }, {})
  const usedLetterCounts = slots.reduce<Record<string, number>>((acc, c) => {
    if (c) acc[c] = (acc[c] ?? 0) + 1
    return acc
  }, {})

  const canAdd = useCallback(
    (char: string) => {
      const used = usedLetterCounts[char] ?? 0
      const max = letterCounts[char] ?? 0
      return used < max
    },
    [usedLetterCounts, letterCounts]
  )

  const handleLetter = useCallback(
    (char: string) => {
      if (phase !== 'playing') return
      if (!canAdd(char)) return
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
    [phase, slots, canAdd, answer]
  )

  const handleBackspace = useCallback(() => {
    if (phase !== 'playing') return
    const idx = slots.map((s, i) => (s ? i : -1)).filter((i) => i >= 0).pop()
    if (idx == null) return
    setSlots((prev) => {
      const next = [...prev]
      next[idx] = ''
      return next
    })
  }, [phase, slots])

  const startGame = useCallback(() => {
    setSlots(Array(len).fill(''))
    setWrongPlacements(0)
    setPhase('playing')
  }, [len])

  useEffect(() => {
    if (phase !== 'playing') return
    const onKey = (e: KeyboardEvent) => {
      if (e.ctrlKey || e.altKey || e.metaKey) return
      if (e.key === 'Backspace') { handleBackspace(); return }
      if (e.key.length === 1) {
        const ch = e.key.toLowerCase()
        if (/[a-zà-ÿœæ]/u.test(ch)) handleLetter(ch)
      }
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [phase, handleLetter, handleBackspace])

  useEffect(() => {
    if (phase !== 'playing' || !isCorrect) return
    setPhase('done')
    const penalty = wrongPlacements * 0.08
    const score = Math.max(0.3, 1 - penalty)
    onComplete?.({
      score,
      subscores: [
        { label: 'Wrong tries', value: wrongPlacements },
        { label: 'Letters', value: len },
      ],
    })
  }, [phase, isCorrect, wrongPlacements, len, onComplete])

  if (phase === 'intro') {
    return (
      <div className="anagram-intro">
        <p className="prompt">{exercise.prompt}</p>
        {showInstruction && (
          <p className="anagram-instruction">
            Recomposez le mot à partir des lettres mélangées.
          </p>
        )}
        <button type="button" onClick={startGame} className="anagram-start-btn" autoFocus>
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
      <p className="anagram-letters-pool">
        Lettres : {letters.join(' ')}
      </p>
      <div className="anagram-grid">
        {slots.map((char, i) => (
          <div
            key={i}
            className={`anagram-slot ${char ? 'filled' : 'empty'}`}
          >
            {char.toUpperCase()}
          </div>
        ))}
      </div>
      <div className="anagram-keyboard">
        {letters
          .filter((c, i) => letters.indexOf(c) === i)
          .map((c) => {
            const total = letterCounts[c] ?? 0
            const used = usedLetterCounts[c] ?? 0
            const remaining = total - used
            const showCount = total > 1
            return (
              <button
                key={c}
                type="button"
                className="anagram-key"
                onClick={() => handleLetter(c)}
                disabled={!canAdd(c)}
              >
                {c.toUpperCase()}
                {showCount && (
                  <span className="anagram-key-count" title={`${remaining} restante(s) sur ${total}`}>
                    {remaining}/{total}
                  </span>
                )}
              </button>
            )
          })}
        <button
          type="button"
          className="anagram-key anagram-key-back"
          onClick={handleBackspace}
        >
          ⌫
        </button>
      </div>
    </div>
  )
}
