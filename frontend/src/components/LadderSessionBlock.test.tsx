import { describe, it, expect, vi, afterEach } from 'vitest'
import { render, screen, waitFor, act } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { LadderSessionBlock } from './LadderSessionBlock'
import type { LadderSessionResponseDto, LadderNextResponseDto, ExerciseDto, LadderStateDto } from '../types/api'

vi.mock('../api/ladder', () => ({
  startLadderSession: vi.fn(),
  getNextLadderExercise: vi.fn(),
}))

import * as ladderApi from '../api/ladder'

afterEach(() => vi.clearAllMocks())

// POST_EXERCISE_DELAY_MS in LadderSessionBlock is 1200 ms.
// Tests that verify auto-advance must allow at least that long.
const POST_EXERCISE_TIMEOUT = 3000

function makeExercise(id: string, prompt: string): ExerciseDto {
  return {
    id,
    subjectId: 'sub-1',
    subjectCode: 'math',
    type: 'FLASHCARD_QA',
    difficulty: 'ULTRA_EASY',
    prompt,
    expectedAnswers: ['42'],
    timeLimitSeconds: 30,
  }
}

function makeState(level = 0, recentScores: number[] = [], overallTotal = 0): LadderStateDto {
  return {
    ladderCode: 'default',
    currentLevelIndex: level,
    recentScores,
    overallScoreSum: recentScores.reduce((a, b) => a + b, 0),
    overallTotal,
  }
}

function renderLadder(ladderCode = 'default') {
  render(
    <MemoryRouter>
      <LadderSessionBlock ladderCode={ladderCode} />
    </MemoryRouter>
  )
}

async function waitForExercise(prompt: string) {
  await waitFor(() => expect(screen.getByText(prompt)).toBeInTheDocument())
}

async function clickCheck() {
  await act(async () => {
    screen.getByRole('button', { name: /check/i }).click()
  })
}

// ---------------------------------------------------------------------------

describe('LadderSessionBlock – continuous play (no blocking score screen)', () => {
  it('shows the first exercise immediately — no score screen on entry', async () => {
    vi.mocked(ladderApi.startLadderSession).mockResolvedValue({
      profileId: 'p1',
      mode: 'ladder',
      exercise: makeExercise('ex-1', 'What is 2 + 2?'),
      ladderState: makeState(),
      sessionDefaultSeconds: 60,
      lowBatteryModeSeconds: 30,
    } satisfies LadderSessionResponseDto)

    renderLadder()
    await waitForExercise('What is 2 + 2?')

    // No blocking score screen, no Continue button on initial load
    expect(screen.queryByText(/Excellent|Well done|Good effort|Keep practicing/)).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /continue/i })).not.toBeInTheDocument()
  })

  it('auto-advances to the next exercise after completion — player never clicks Continue', async () => {
    vi.mocked(ladderApi.startLadderSession).mockResolvedValue({
      profileId: 'p1',
      mode: 'ladder',
      exercise: makeExercise('ex-1', 'First question'),
      ladderState: makeState(0, [], 0),
      sessionDefaultSeconds: 60,
      lowBatteryModeSeconds: 30,
    } satisfies LadderSessionResponseDto)

    vi.mocked(ladderApi.getNextLadderExercise).mockResolvedValue({
      exercise: makeExercise('ex-2', 'Second question'),
      ladderState: makeState(0, [0], 1),
    } satisfies LadderNextResponseDto)

    renderLadder()
    await waitForExercise('First question')

    // Submit the exercise (empty answer → score 0)
    await clickCheck()

    // API called automatically — no Continue button
    await waitFor(() => expect(vi.mocked(ladderApi.getNextLadderExercise)).toHaveBeenCalledTimes(1))
    expect(vi.mocked(ladderApi.getNextLadderExercise)).toHaveBeenCalledWith(makeState(0, [], 0), 0)

    // Next exercise appears without any Continue button click
    await waitFor(
      () => expect(screen.getByText('Second question')).toBeInTheDocument(),
      { timeout: POST_EXERCISE_TIMEOUT }
    )
    expect(screen.queryByRole('button', { name: /continue/i })).not.toBeInTheDocument()
  }, 8000)

  it('advances even when the backend returns the same exercise ID (math re-roll)', async () => {
    const SAME_ID = 'math-ex-1'
    vi.mocked(ladderApi.startLadderSession).mockResolvedValue({
      profileId: 'p1',
      mode: 'ladder',
      exercise: makeExercise(SAME_ID, 'What is 3 + 5?'),
      ladderState: makeState(0, [], 0),
      sessionDefaultSeconds: 60,
      lowBatteryModeSeconds: 30,
    } satisfies LadderSessionResponseDto)

    // Backend returns a different prompt but the same entity ID (common for math)
    vi.mocked(ladderApi.getNextLadderExercise).mockResolvedValue({
      exercise: makeExercise(SAME_ID, 'What is 7 + 2?'),
      ladderState: makeState(0, [0], 1),
    } satisfies LadderNextResponseDto)

    renderLadder()
    await waitForExercise('What is 3 + 5?')

    const checkBefore = screen.getByRole('button', { name: /check/i })
    await act(async () => { checkBefore.click() })

    // Despite same exercise ID, the component must remount (exerciseKey counter)
    // and present a fresh, unanswered exercise
    await waitFor(
      () => expect(screen.getByText('What is 7 + 2?')).toBeInTheDocument(),
      { timeout: POST_EXERCISE_TIMEOUT }
    )

    // The new exercise must have a fresh Check button (not in revealed state)
    expect(screen.getByRole('button', { name: /check/i })).toBeInTheDocument()
  }, 8000)

  it('shows a non-blocking level-up toast and continues with the new exercise', async () => {
    vi.mocked(ladderApi.startLadderSession).mockResolvedValue({
      profileId: 'p1',
      mode: 'ladder',
      exercise: makeExercise('ex-1', 'Level 0 question'),
      ladderState: makeState(0, [1, 1, 1, 1], 4),
      sessionDefaultSeconds: 60,
      lowBatteryModeSeconds: 30,
    } satisfies LadderSessionResponseDto)

    vi.mocked(ladderApi.getNextLadderExercise).mockResolvedValue({
      exercise: makeExercise('ex-2', 'Level 1 question'),
      ladderState: makeState(1, [], 5),
      levelChanged: { from: 0, to: 1, direction: 'up' },
    } satisfies LadderNextResponseDto)

    renderLadder()
    await waitForExercise('Level 0 question')
    await clickCheck()

    // Next exercise loads — play is uninterrupted
    await waitFor(
      () => expect(screen.getByText('Level 1 question')).toBeInTheDocument(),
      { timeout: POST_EXERCISE_TIMEOUT }
    )

    // Toast is visible (aria live region) with level info — not blocking
    const toast = screen.getByRole('status')
    expect(toast.textContent).toContain('Level 1')

    // No blocking Continue button at any point
    expect(screen.queryByRole('button', { name: /continue/i })).not.toBeInTheDocument()
  }, 8000)

  it('score-animation (.score-animation class) is never rendered in the ladder flow', async () => {
    vi.mocked(ladderApi.startLadderSession).mockResolvedValue({
      profileId: 'p1',
      mode: 'ladder',
      exercise: makeExercise('ex-1', 'Focus question'),
      ladderState: makeState(),
      sessionDefaultSeconds: 60,
      lowBatteryModeSeconds: 30,
    } satisfies LadderSessionResponseDto)

    vi.mocked(ladderApi.getNextLadderExercise).mockResolvedValue({
      exercise: makeExercise('ex-2', 'Next focus question'),
      ladderState: makeState(0, [0], 1),
    } satisfies LadderNextResponseDto)

    renderLadder()
    await waitForExercise('Focus question')

    expect(document.querySelector('.score-animation')).not.toBeInTheDocument()

    await clickCheck()

    await waitFor(
      () => expect(screen.getByText('Next focus question')).toBeInTheDocument(),
      { timeout: POST_EXERCISE_TIMEOUT }
    )

    expect(document.querySelector('.score-animation')).not.toBeInTheDocument()
    expect(screen.queryByText(/Excellent|Well done|Good effort|Keep practicing/)).not.toBeInTheDocument()
  }, 8000)
})
