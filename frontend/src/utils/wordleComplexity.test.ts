import { describe, expect, it } from 'vitest'
import { computeWordleComplexity } from './wordleComplexity'

describe('computeWordleComplexity', () => {
  it('matches seeded FR ladder scores', () => {
    expect(computeWordleComplexity({ wordLength: 3, maxAttempts: 6, timeLimitSeconds: 120, language: 'fr' }).difficultyScore0To100).toBe(25)
    expect(computeWordleComplexity({ wordLength: 5, maxAttempts: 6, timeLimitSeconds: 180, language: 'fr' }).difficultyScore0To100).toBe(43)
    expect(computeWordleComplexity({ wordLength: 6, maxAttempts: 6, timeLimitSeconds: 240, language: 'fr' }).difficultyScore0To100).toBe(48)
    expect(computeWordleComplexity({ wordLength: 7, maxAttempts: 6, timeLimitSeconds: 300, language: 'fr' }).difficultyScore0To100).toBe(53)
  })

  it('longer words score higher than short words with same attempts and time', () => {
    const a = computeWordleComplexity({ wordLength: 3, maxAttempts: 6, timeLimitSeconds: 180, language: 'en' })
    const b = computeWordleComplexity({ wordLength: 7, maxAttempts: 6, timeLimitSeconds: 180, language: 'en' })
    expect(b.difficultyScore0To100).toBeGreaterThan(a.difficultyScore0To100)
  })
})
