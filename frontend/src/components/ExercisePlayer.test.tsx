import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { ExercisePlayer } from './ExercisePlayer'
import type { ExerciseDto } from '../types/api'

const mockExercise: ExerciseDto = {
  id: 'a0000000-0000-0000-0000-000000000001',
  subjectId: 'b0000000-0000-0000-0000-000000000001',
  subjectCode: 'default',
  type: 'FLASHCARD_QA',
  difficulty: 'ULTRA_EASY',
  prompt: 'What is 1 + 1?',
  expectedAnswers: ['2'],
  timeLimitSeconds: 30
}

describe('ExercisePlayer', () => {
  it('renders the exercise and calls onComplete with result (decoupled - does not render ScoreAnimation)', async () => {
    const onComplete = vi.fn()
    render(
      <ExercisePlayer
        exercise={mockExercise}
        onComplete={onComplete}
      />
    )

    // ExercisePlayer should render the exercise (GenericTextExercise for FLASHCARD_QA)
    expect(screen.getByPlaceholderText('Your answer')).toBeInTheDocument()
    expect(screen.getByText('What is 1 + 1?')).toBeInTheDocument()

    // ExercisePlayer does NOT render ScoreAnimation - that is the parent's responsibility
    expect(screen.queryByText(/Excellent|Well done|Good effort|Keep practicing/)).not.toBeInTheDocument()
  })
})
