import { describe, expect, it } from 'vitest'
import { targetScoreForLevel } from './offlineLadderEngine'

describe('targetScoreForLevel', () => {
  it('decreases quickly early then linearly', () => {
    const max = 29
    const l0 = targetScoreForLevel(0, max, ['ULTRA_EASY'])
    const l3 = targetScoreForLevel(3, max, ['ULTRA_EASY'])
    const l6 = targetScoreForLevel(6, max, ['EASY'])
    const l15 = targetScoreForLevel(15, max, ['MEDIUM'])
    const l29 = targetScoreForLevel(29, max, ['VERY_HARD'])

    expect(l0).toBeGreaterThan(l3)
    expect(l3).toBeGreaterThan(l6)
    expect(l6).toBeGreaterThan(l15)
    expect(l15).toBeGreaterThan(l29)

    // First segment should drop faster than later linear segment.
    const earlyPerLevelDrop = (l0 - l3) / 3
    const latePerLevelDrop = (l15 - l29) / 14
    expect(earlyPerLevelDrop).toBeGreaterThan(latePerLevelDrop)
  })
})
