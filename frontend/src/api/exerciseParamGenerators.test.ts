import { describe, expect, it } from 'vitest'
import {
  estimateDigitSpanComplexityScore,
  estimateGeneratedScore,
  estimateNBackComplexityScore,
  generateParamsFromScore,
  listExerciseParamGenerators,
} from './exerciseParamGenerators'

describe('exerciseParamGenerators', () => {
  it('registers score-driven generators for supported exercise types', () => {
    expect(listExerciseParamGenerators().sort()).toEqual(
      [
        'ANAGRAM',
        'DIGIT_SPAN',
        'DUAL_NBACK',
        'ESTIMATION',
        'FLASHCARD_QA',
        'IMAGE_PAIR',
        'MATH_CHAIN',
        'MEMORY_CARD_PAIRS',
        'N_BACK',
        'SUM_PAIR',
        'WORDLE',
      ].sort(),
    )
  })

  it('clamps out-of-range target scores', () => {
    const low = generateParamsFromScore('SUM_PAIR', -10)
    const high = generateParamsFromScore('SUM_PAIR', 10)
    expect(low.type).toBe('SUM_PAIR')
    expect(high.type).toBe('SUM_PAIR')
  })

  it('is monotonic for SUM_PAIR (higher target score => easier predicted params)', () => {
    const hard = generateParamsFromScore('SUM_PAIR', 0.35)
    const medium = generateParamsFromScore('SUM_PAIR', 0.6)
    const easy = generateParamsFromScore('SUM_PAIR', 0.8)
    const hardScore = estimateGeneratedScore(hard)
    const mediumScore = estimateGeneratedScore(medium)
    const easyScore = estimateGeneratedScore(easy)
    expect(mediumScore).toBeGreaterThanOrEqual(hardScore)
    expect(easyScore).toBeGreaterThanOrEqual(mediumScore)
  })

  it('keeps SUM_PAIR generated score reasonably close to target', () => {
    const targets = [0.82, 0.72, 0.68, 0.53]
    for (const target of targets) {
      const params = generateParamsFromScore('SUM_PAIR', target)
      const estimated = estimateGeneratedScore(params)
      expect(Math.abs(estimated - target)).toBeLessThanOrEqual(0.12)
    }
  })

  it('keeps MEMORY_CARD_PAIRS generated score reasonably close to target', () => {
    const targets = [0.8, 0.65, 0.5, 0.35]
    for (const target of targets) {
      const params = generateParamsFromScore('MEMORY_CARD_PAIRS', target)
      const estimated = estimateGeneratedScore(params)
      expect(Math.abs(estimated - target)).toBeLessThanOrEqual(0.12)
    }
  })

  it('keeps IMAGE_PAIR generated score reasonably close to target', () => {
    const targets = [0.78, 0.6, 0.45, 0.3]
    for (const target of targets) {
      const params = generateParamsFromScore('IMAGE_PAIR', target)
      const estimated = estimateGeneratedScore(params)
      expect(Math.abs(estimated - target)).toBeLessThanOrEqual(0.12)
    }
  })

  it('generates easy WORDLE params for high target score', () => {
    const params = generateParamsFromScore('WORDLE', 0.82)
    expect(params.type).toBe('WORDLE')
    expect(params.params.wordLength).toBeLessThanOrEqual(4)
    expect((params.params.maxAttempts ?? 6)).toBeGreaterThanOrEqual(6)
  })

  it('generates hard WORDLE params for low target score', () => {
    const params = generateParamsFromScore('WORDLE', 0.33)
    expect(params.type).toBe('WORDLE')
    expect(params.params.wordLength).toBeGreaterThanOrEqual(6)
    expect((params.params.maxAttempts ?? 6)).toBeLessThanOrEqual(5)
  })

  it('generates easy ANAGRAM params for high target score', () => {
    const params = generateParamsFromScore('ANAGRAM', 0.8)
    expect(params.type).toBe('ANAGRAM')
    expect(params.params.answer.length).toBeLessThanOrEqual(4)
    expect(params.params.scrambledLetters).toHaveLength(params.params.answer.length)
  })

  it('generates hard ANAGRAM params for low target score', () => {
    const params = generateParamsFromScore('ANAGRAM', 0.3)
    expect(params.type).toBe('ANAGRAM')
    expect(params.params.answer.length).toBeGreaterThanOrEqual(6)
  })

  it('keeps ESTIMATION generated score within 5%', () => {
    const targets = [0.85, 0.7, 0.5, 0.3, 0.15]
    for (const target of targets) {
      const params = generateParamsFromScore('ESTIMATION', target)
      const estimated = estimateGeneratedScore(params)
      expect(Math.abs(estimated - target)).toBeLessThanOrEqual(0.05)
    }
  })

  it('generates stricter estimation tolerance for harder target', () => {
    const easy = generateParamsFromScore('ESTIMATION', 0.85)
    const hard = generateParamsFromScore('ESTIMATION', 0.2)
    expect(easy.type).toBe('ESTIMATION')
    expect(hard.type).toBe('ESTIMATION')
    expect(hard.params.toleranceFactor).toBeLessThanOrEqual(easy.params.toleranceFactor)
  })

  it('maps N-BACK and DIGIT_SPAN anchor formulas to 0..100', () => {
    expect(estimateNBackComplexityScore(1, 1, 2)).toBeCloseTo(0, 6)
    expect(estimateNBackComplexityScore(4, 2, 4)).toBeCloseTo(100, 6)
    expect(estimateDigitSpanComplexityScore(3)).toBe(0)
    expect(estimateDigitSpanComplexityScore(10)).toBe(100)
  })

  it('keeps N_BACK, DUAL_NBACK and DIGIT_SPAN generated scores reasonably close', () => {
    const target = 0.4
    const nBack = generateParamsFromScore('N_BACK', target)
    const dual = generateParamsFromScore('DUAL_NBACK', target)
    const digitSpan = generateParamsFromScore('DIGIT_SPAN', target)
    expect(Math.abs(estimateGeneratedScore(nBack) - target)).toBeLessThanOrEqual(0.15)
    expect(Math.abs(estimateGeneratedScore(dual) - target)).toBeLessThanOrEqual(0.15)
    expect(Math.abs(estimateGeneratedScore(digitSpan) - target)).toBeLessThanOrEqual(0.15)
  })
})
