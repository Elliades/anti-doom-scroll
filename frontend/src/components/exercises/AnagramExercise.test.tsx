import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent, act } from '@testing-library/react'
import { AnagramExercise } from './AnagramExercise'
import type { ExerciseDto } from '../../types/api'

const anagramExercise = (
  overrides: Partial<NonNullable<ExerciseDto['anagramParams']>> = {}
): ExerciseDto =>
  ({
    id: 'ex-1',
    subjectId: 'sub-1',
    subjectCode: 'anagram',
    type: 'ANAGRAM',
    difficulty: 'EASY',
    prompt: 'Trouvez le mot',
    expectedAnswers: [],
    timeLimitSeconds: 60,
    anagramParams: {
      scrambledLetters: ['c', 'h', 'a', 't'],
      answer: 'chat',
      ...overrides,
    },
  }) as ExerciseDto

describe('AnagramExercise', () => {
  it('renders error when params are missing', () => {
    const ex = { ...anagramExercise(), anagramParams: undefined } as ExerciseDto
    render(<AnagramExercise exercise={ex} />)
    expect(screen.getByText(/invalid anagram exercise/i)).toBeInTheDocument()
  })

  it('renders intro screen with start button', () => {
    render(<AnagramExercise exercise={anagramExercise()} />)
    expect(screen.getByText('Trouvez le mot')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /commencer/i })).toBeInTheDocument()
  })

  it('transitions to playing phase after clicking start', async () => {
    render(<AnagramExercise exercise={anagramExercise()} />)
    await act(() => {
      fireEvent.click(screen.getByRole('button', { name: /commencer/i }))
    })
    expect(screen.getByText(/lettres/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '⌫' })).toBeInTheDocument()
  })

  it('shows keyboard buttons for each distinct letter', async () => {
    render(<AnagramExercise exercise={anagramExercise()} />)
    await act(() => {
      fireEvent.click(screen.getByRole('button', { name: /commencer/i }))
    })
    expect(screen.getByRole('button', { name: /^C$/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /^H$/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /^A$/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /^T$/i })).toBeInTheDocument()
  })

  it('completes when all correct letters are entered via click', async () => {
    const onComplete = vi.fn()
    render(<AnagramExercise exercise={anagramExercise()} onComplete={onComplete} />)
    await act(() => {
      fireEvent.click(screen.getByRole('button', { name: /commencer/i }))
    })
    for (const letter of ['C', 'H', 'A', 'T']) {
      await act(() => {
        fireEvent.click(screen.getByRole('button', { name: new RegExp(`^${letter}$`, 'i') }))
      })
    }
    expect(onComplete).toHaveBeenCalledWith(
      expect.objectContaining({
        score: expect.any(Number),
        subscores: expect.arrayContaining([
          expect.objectContaining({ label: 'Wrong tries', value: 0 }),
        ]),
      })
    )
  })

  it('completes when correct letters are entered via keyboard', async () => {
    const onComplete = vi.fn()
    render(<AnagramExercise exercise={anagramExercise()} onComplete={onComplete} />)
    await act(() => {
      fireEvent.click(screen.getByRole('button', { name: /commencer/i }))
    })
    for (const key of ['c', 'h', 'a', 't']) {
      await act(() => {
        fireEvent.keyDown(window, { key })
      })
    }
    expect(onComplete).toHaveBeenCalledWith(
      expect.objectContaining({ score: 1 })
    )
  })

  it('backspace removes the last entered letter', async () => {
    render(<AnagramExercise exercise={anagramExercise()} />)
    await act(() => {
      fireEvent.click(screen.getByRole('button', { name: /commencer/i }))
    })
    await act(() => {
      fireEvent.keyDown(window, { key: 'c' })
    })
    const filledSlots = () => document.querySelectorAll('.anagram-slot.filled')
    expect(filledSlots()).toHaveLength(1)
    await act(() => {
      fireEvent.keyDown(window, { key: 'Backspace' })
    })
    expect(filledSlots()).toHaveLength(0)
  })

  it('backspace button removes the last entered letter', async () => {
    render(<AnagramExercise exercise={anagramExercise()} />)
    await act(() => {
      fireEvent.click(screen.getByRole('button', { name: /commencer/i }))
    })
    await act(() => {
      fireEvent.click(screen.getByRole('button', { name: /^C$/i }))
    })
    expect(document.querySelectorAll('.anagram-slot.filled')).toHaveLength(1)
    await act(() => {
      fireEvent.click(screen.getByRole('button', { name: '⌫' }))
    })
    expect(document.querySelectorAll('.anagram-slot.filled')).toHaveLength(0)
  })

  it('disables keyboard button when all instances of a letter are used', async () => {
    render(<AnagramExercise exercise={anagramExercise()} />)
    await act(() => {
      fireEvent.click(screen.getByRole('button', { name: /commencer/i }))
    })
    const cBtn = screen.getByRole('button', { name: /^C$/i })
    expect(cBtn).not.toBeDisabled()
    await act(() => {
      fireEvent.click(cBtn)
    })
    expect(cBtn).toBeDisabled()
  })

  it('tracks wrong placements when a wrong letter is placed', async () => {
    const onComplete = vi.fn()
    const ex = anagramExercise({ scrambledLetters: ['h', 'c', 'a', 't'], answer: 'chat' })
    render(<AnagramExercise exercise={ex} onComplete={onComplete} />)
    await act(() => {
      fireEvent.click(screen.getByRole('button', { name: /commencer/i }))
    })
    // Type 'h' first — wrong position (answer starts with 'c')
    await act(() => { fireEvent.keyDown(window, { key: 'h' }) })
    await act(() => { fireEvent.keyDown(window, { key: 'Backspace' }) })
    // Now type correct answer
    for (const key of ['c', 'h', 'a', 't']) {
      await act(() => { fireEvent.keyDown(window, { key }) })
    }
    expect(onComplete).toHaveBeenCalledWith(
      expect.objectContaining({
        subscores: expect.arrayContaining([
          expect.objectContaining({ label: 'Wrong tries', value: 1 }),
        ]),
      })
    )
  })

  it('shows duplicate letter count on keyboard', async () => {
    const ex = anagramExercise({ scrambledLetters: ['l', 'l', 'e', 'b'], answer: 'bell' })
    render(<AnagramExercise exercise={ex} />)
    await act(() => {
      fireEvent.click(screen.getByRole('button', { name: /commencer/i }))
    })
    expect(screen.getByTitle(/2 restante\(s\) sur 2/)).toBeInTheDocument()
  })

  it('does not crash on render (no TDZ error)', () => {
    expect(() => {
      render(<AnagramExercise exercise={anagramExercise()} />)
    }).not.toThrow()
  })
})
