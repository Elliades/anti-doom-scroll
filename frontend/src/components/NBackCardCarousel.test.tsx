import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { render, act } from '@testing-library/react'
import { NBackCardCarousel } from './NBackCardCarousel'

// Timing from NBackCardCarousel: INITIAL_DELAY_MS=400, FLIP_MS=256, FACE_DISPLAY_MS=500, MOVE_MS=360
// First card: onCardShow(0) at 400ms (start of first advance)
// Second card: onCardShow(1) at 400 + 1116 = 1516ms (t3)
// Third card: onCardShow(2) at 400 + 1976 + 1116 = 3492ms (t3 of second advance)
const INITIAL_DELAY_MS = 400
const T3_OFFSET_MS = 256 + 500 + 360 // FLIP_MS + FACE_DISPLAY_MS + MOVE_MS = 1116
const CYCLE_MS = 256 + 500 + 360 * 2 + 500 // time until next advance() = 1976

describe('NBackCardCarousel', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })
  afterEach(() => {
    vi.useRealTimers()
  })

  it('calls onCardShow with current index when each new card is shown (sync with animation)', () => {
    const onCardShow = vi.fn()
    const sequence = ['AC', '2D', '3H', '4C']

    render(
      <NBackCardCarousel
        n={1}
        sequence={sequence}
        onCardShow={onCardShow}
      />
    )

    // Before initial delay: no card shown yet
    expect(onCardShow).not.toHaveBeenCalled()

    // After initial delay: first card (index 0) is shown
    act(() => { vi.advanceTimersByTime(INITIAL_DELAY_MS) })
    expect(onCardShow).toHaveBeenCalledTimes(1)
    expect(onCardShow).toHaveBeenLastCalledWith(0)

    // When t3 runs: second card (index 1) appears in center → onCardShow(1)
    act(() => { vi.advanceTimersByTime(T3_OFFSET_MS) })
    expect(onCardShow).toHaveBeenCalledTimes(2)
    expect(onCardShow).toHaveBeenLastCalledWith(1)

    // Next cycle t3: third card (index 2)
    act(() => { vi.advanceTimersByTime(CYCLE_MS) })
    expect(onCardShow).toHaveBeenCalledTimes(3)
    expect(onCardShow).toHaveBeenLastCalledWith(2)

    // Next cycle t3: fourth card (index 3)
    act(() => { vi.advanceTimersByTime(CYCLE_MS) })
    expect(onCardShow).toHaveBeenCalledTimes(4)
    expect(onCardShow).toHaveBeenLastCalledWith(3)

    // Sequence complete; no more onCardShow for new cards (onComplete is called instead)
    act(() => { vi.advanceTimersByTime(CYCLE_MS + 1000) })
    expect(onCardShow).toHaveBeenCalledTimes(4)
  })
})
