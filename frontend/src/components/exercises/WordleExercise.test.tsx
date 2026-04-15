import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, act } from '@testing-library/react'
import { WordleExercise, normalizeForCompare, getTileStates } from './WordleExercise'
import type { ExerciseDto } from '../../types/api'

const wordleExercise = (overrides: Partial<ExerciseDto['wordleParams']> = {}): ExerciseDto =>
  ({
    id: 'ex-1',
    subjectId: 'sub-1',
    subjectCode: 'wordle',
    type: 'WORDLE',
    difficulty: 'EASY',
    prompt: 'Devine le mot',
    expectedAnswers: [],
    timeLimitSeconds: 60,
    wordleParams: {
      answer: 'café',
      wordLength: 4,
      maxAttempts: 6,
      language: 'fr',
      ...overrides,
    },
  }) as ExerciseDto

describe('normalizeForCompare', () => {
  it('strips accents: é, è, ê → e', () => {
    expect(normalizeForCompare('é')).toBe('e')
    expect(normalizeForCompare('è')).toBe('e')
    expect(normalizeForCompare('ê')).toBe('e')
    expect(normalizeForCompare('café')).toBe('cafe')
  })

  it('strips accents: à, â → a', () => {
    expect(normalizeForCompare('à')).toBe('a')
    expect(normalizeForCompare('â')).toBe('a')
  })

  it('strips accents: ù, û, î, ô, ç', () => {
    expect(normalizeForCompare('ù')).toBe('u')
    expect(normalizeForCompare('û')).toBe('u')
    expect(normalizeForCompare('î')).toBe('i')
    expect(normalizeForCompare('ô')).toBe('o')
    expect(normalizeForCompare('ç')).toBe('c')
  })

  it('replaces œ with oe and æ with ae', () => {
    expect(normalizeForCompare('œ')).toBe('oe')
    expect(normalizeForCompare('cœur')).toBe('coeur')
    expect(normalizeForCompare('æ')).toBe('ae')
  })

  it('leaves plain letters unchanged', () => {
    expect(normalizeForCompare('hello')).toBe('hello')
    expect(normalizeForCompare('cafe')).toBe('cafe')
  })

  it('is case-insensitive (lowercases)', () => {
    expect(normalizeForCompare('Café')).toBe('cafe')
  })
})

describe('getTileStates', () => {
  it('marks all correct when guess matches normalized answer', () => {
    const states = getTileStates('cafe', 'cafe')
    expect(states).toEqual(['correct', 'correct', 'correct', 'correct'])
  })

  it('marks correct / present / absent for mixed guess', () => {
    const states = getTileStates('care', 'cafe')
    expect(states[0]).toBe('correct') // c
    expect(states[1]).toBe('correct') // a
    expect(states[2]).toBe('absent')  // r not in answer
    expect(states[3]).toBe('correct') // e
  })

  it('handles repeated letters in answer', () => {
    const states = getTileStates('elle', 'bell')
    // e→present (e in bell), l→present (l in bell), l→correct, e→absent (e already used)
    expect(states).toEqual(['present', 'present', 'correct', 'absent'])
  })
})

describe('WordleExercise', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  it('shows Enter/Entrée button on keyboard', () => {
    render(<WordleExercise exercise={wordleExercise()} />)
    expect(screen.getByRole('button', { name: /entrée|enter/i })).toBeInTheDocument()
  })

  it('does not show accent keys on French keyboard (words are unaccented)', () => {
    render(<WordleExercise exercise={wordleExercise({ language: 'fr' })} />)
    expect(screen.queryByRole('button', { name: 'É' })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'À' })).not.toBeInTheDocument()
  })

  it('submits when Enter is pressed after a full valid word (e.g. 3 letters)', async () => {
    const onComplete = vi.fn()
    render(
      <WordleExercise
        exercise={wordleExercise({ answer: 'oui', wordLength: 3 })}
        onComplete={onComplete}
      />
    )
    await act(() => {
      fireEvent.click(screen.getByRole('button', { name: 'O' }))
    })
    await act(() => {
      fireEvent.click(screen.getByRole('button', { name: 'U' }))
    })
    expect(onComplete).not.toHaveBeenCalled()
    await act(() => {
      fireEvent.click(screen.getByRole('button', { name: 'I' }))
    })
    await act(() => {
      fireEvent.click(screen.getByRole('button', { name: /entrée|enter/i }))
    })
    await act(() => {
      vi.advanceTimersByTime(2000)
    })
    expect(onComplete).toHaveBeenCalledWith(
      expect.objectContaining({
        score: expect.any(Number),
        subscores: expect.any(Array),
      })
    )
  })

  it('treats base letter "e" as matching accented "é" in answer', async () => {
    const onComplete = vi.fn()
    render(
      <WordleExercise
        exercise={wordleExercise({ answer: 'café', wordLength: 4 })}
        onComplete={onComplete}
      />
    )
    await act(() => {
      fireEvent.click(screen.getByRole('button', { name: 'C' }))
    })
    await act(() => {
      fireEvent.click(screen.getByRole('button', { name: 'A' }))
    })
    await act(() => {
      fireEvent.click(screen.getByRole('button', { name: 'F' }))
    })
    await act(() => {
      fireEvent.click(screen.getByRole('button', { name: 'E' }))
    })
    await act(() => {
      fireEvent.click(screen.getByRole('button', { name: /entrée|enter/i }))
    })
    await act(() => {
      vi.runAllTimers()
    })
    expect(onComplete).toHaveBeenCalledWith(
      expect.objectContaining({
        score: expect.any(Number),
      })
    )
  })

  it('shows Backspace key on keyboard', () => {
    render(<WordleExercise exercise={wordleExercise()} />)
    const backspace = screen.getByRole('button', { name: '⌫' })
    expect(backspace).toBeInTheDocument()
  })
})
