import type { WordleComplexityDto } from '../types/api'

/**
 * Structural Wordle difficulty (mirrors Kotlin WordleComplexity).
 * difficultyScore0To100: 0 = easiest supported setup, 100 = hardest.
 */

const MIN_LEN = 3
const MAX_LEN = 10
const MIN_ATTEMPTS = 3
const MAX_ATTEMPTS = 10
const REF_TIME_CAP_SECONDS = 300

export function defaultWordleAlphabetSize(language: string): number {
  return language.toLowerCase() === 'fr' ? 27 : 26
}

export function computeWordleComplexity(input: {
  wordLength: number
  maxAttempts: number
  timeLimitSeconds: number
  language: string
}): WordleComplexityDto {
  const L = Math.min(MAX_LEN, Math.max(MIN_LEN, input.wordLength))
  const G = Math.min(MAX_ATTEMPTS, Math.max(MIN_ATTEMPTS, input.maxAttempts))
  const T = Math.max(1, input.timeLimitSeconds)
  const A = defaultWordleAlphabetSize(input.language)
  const spaceLog10 = L * Math.log10(A)
  const entropy = (L * Math.log(A)) / Math.LN2
  const guessesPerLetter = G / L
  const secondsPerGuessBudget = T / G

  const entropyMin = (MIN_LEN * Math.log(26)) / Math.LN2
  const entropyMax = (MAX_LEN * Math.log(27)) / Math.LN2
  const entropyNorm = Math.min(1, Math.max(0, (entropy - entropyMin) / (entropyMax - entropyMin)))

  const ratioMin = MIN_ATTEMPTS / MAX_LEN
  const ratioMax = MAX_ATTEMPTS / MIN_LEN
  const attemptTightness =
    1 - Math.min(1, Math.max(0, (guessesPerLetter - ratioMin) / (ratioMax - ratioMin)))

  const timePressure = 1 - Math.min(1, Math.max(0, T / REF_TIME_CAP_SECONDS))

  const raw = 0.52 * entropyNorm + 0.28 * attemptTightness + 0.2 * timePressure
  const difficultyScore0To100 = Math.min(100, Math.max(0, Math.round(raw * 100)))

  return {
    wordLength: L,
    maxAttempts: G,
    timeLimitSeconds: T,
    effectiveAlphabetSize: A,
    searchSpaceLog10: spaceLog10,
    entropyBits: entropy,
    guessesPerLetter,
    secondsPerGuessBudget,
    difficultyScore0To100,
  }
}
