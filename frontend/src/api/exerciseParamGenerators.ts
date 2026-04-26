import type {
  AnagramParamsDto,
  EstimationParamsDto,
  DigitSpanParamsDto,
  ImagePairParamsDto,
  MemoryCardParamsDto,
  NBackParamsDto,
  SumPairParamsDto,
  WordleParamsDto,
} from '../types/api'
import {
  anagramComplexityScore,
  lexiconForLanguage,
  type RankedWord,
  wordleComplexityScore,
} from '../utils/wordComplexity'

type SupportedExerciseType =
  | 'SUM_PAIR'
  | 'MEMORY_CARD_PAIRS'
  | 'IMAGE_PAIR'
  | 'FLASHCARD_QA'
  | 'MATH_CHAIN'
  | 'WORDLE'
  | 'ANAGRAM'
  | 'ESTIMATION'
  | 'N_BACK'
  | 'DUAL_NBACK'
  | 'DIGIT_SPAN'

export type ExerciseParams =
  | {
      type: 'SUM_PAIR'
      params: SumPairParamsDto
    }
  | {
      type: 'MEMORY_CARD_PAIRS'
      params: MemoryCardParamsDto
    }
  | {
      type: 'IMAGE_PAIR'
      params: ImagePairParamsDto
    }
  | {
      type: 'FLASHCARD_QA'
      params: {
        operation: string
        firstOperand: number
        secondOperand: number
        complexityScore: number
      }
    }
  | {
      type: 'MATH_CHAIN'
      params: {
        startNumber: number
        steps: Array<{
          operation: 'ADD' | 'SUBTRACT' | 'MULTIPLY' | 'DIVIDE'
          operand: number
        }>
        complexityScore: number
      }
    }
  | {
      type: 'WORDLE'
      params: WordleParamsDto
    }
  | {
      type: 'ANAGRAM'
      params: AnagramParamsDto
    }
  | {
      type: 'ESTIMATION'
      params: EstimationParamsDto & { complexityScore: number }
    }
  | {
      type: 'N_BACK'
      params: Pick<NBackParamsDto, 'n'> & {
        suitCount: number
        complexityScore: number
      }
    }
  | {
      type: 'DUAL_NBACK'
      params: {
        n: number
        gridSize: number
        colorCount: number
        complexityScore: number
      }
    }
  | {
      type: 'DIGIT_SPAN'
      params: Pick<DigitSpanParamsDto, 'startLength'> & {
        complexityScore: number
      }
    }

export interface ExerciseParamGenerator<T extends ExerciseParams> {
  readonly type: T['type']
  generateParamsFromScore(targetScore: number): T
}

const SCORE_MAX_ATTEMPTS = 300
const RAW_SCORE_MIN = 2 // "2 + 2" -> (1 + 1) * 1 + (0 * 2) = 2
const RAW_SCORE_MAX = 51 // "(998 * 76) / 4" anchor for 100
const COMPLEXITY_TOLERANCE_POINTS = 5
const ESTIMATION_TOLERANCE_MIN = 0.05
const ESTIMATION_TOLERANCE_MAX = 0.5
const ESTIMATION_TOLERANCE_STEP = 0.005
const ESTIMATION_CATEGORY_WEIGHTS = {
  SIMPLE: 1.0,
  EVERYDAY: 1.8,
  SCIENCE: 2.8,
  BUDGET: 4.0,
} as const
const ESTIMATION_RAW_MIN = (1 / ESTIMATION_TOLERANCE_MAX) * ESTIMATION_CATEGORY_WEIGHTS.SIMPLE
const ESTIMATION_RAW_MAX = (1 / ESTIMATION_TOLERANCE_MIN) * ESTIMATION_CATEGORY_WEIGHTS.BUDGET
const NBACK_RAW_MIN = 1
const NBACK_RAW_MAX = 16

function clampScore(targetScore: number): number {
  if (!Number.isFinite(targetScore)) return 0.5
  return Math.max(0, Math.min(1, targetScore))
}

function randInt(min: number, max: number): number {
  return min + Math.floor(Math.random() * (max - min + 1))
}

function digitCount(n: number): number {
  return Math.abs(Math.trunc(n)).toString().length
}

/**
 * Miller-inspired chunk load estimator.
 * Formula: chunkLoad = digits(operandA) + digits(operandB)
 *
 * Rationale: each extra digit increases simultaneous items in working memory
 * (Miller, 1956: bounded short-term memory capacity).
 */
function chunkLoadForOperands(a: number, b: number): number {
  return digitCount(a) + digitCount(b)
}

/**
 * Zipf-like operation-frequency weighting.
 * Formula: Cb_op = chunkLoad * opWeight
 *
 * Weights follow cognitive rarity/cost assumptions:
 * + = 1.0, - = 1.2, * = 2.0, / = 2.5
 * (rarer operators tend to require more deliberate processing).
 */
function operationWeight(op: 'ADD' | 'SUBTRACT' | 'MULTIPLY' | 'DIVIDE'): number {
  if (op === 'ADD') return 1
  if (op === 'SUBTRACT') return 1.2
  if (op === 'MULTIPLY') return 2
  return 2.5
}

function countAdditionCarries(a: number, b: number): number {
  let x = Math.abs(Math.trunc(a))
  let y = Math.abs(Math.trunc(b))
  let carry = 0
  let total = 0
  while (x > 0 || y > 0 || carry > 0) {
    const sum = (x % 10) + (y % 10) + carry
    carry = sum >= 10 ? 1 : 0
    if (carry > 0) total++
    x = Math.floor(x / 10)
    y = Math.floor(y / 10)
  }
  return total
}

function countSubtractionBorrows(a: number, b: number): number {
  let x = Math.abs(Math.trunc(a))
  let y = Math.abs(Math.trunc(b))
  let borrow = 0
  let total = 0
  while (x > 0 || y > 0) {
    const da = (x % 10) - borrow
    const db = y % 10
    if (da < db) {
      total++
      borrow = 1
    } else {
      borrow = 0
    }
    x = Math.floor(x / 10)
    y = Math.floor(y / 10)
  }
  return total
}

function countMultiplicationCarries(a: number, b: number): number {
  const ad = Math.abs(Math.trunc(a)).toString().split('').reverse().map(Number)
  const bd = Math.abs(Math.trunc(b)).toString().split('').reverse().map(Number)
  let carries = 0
  for (const db of bd) {
    let carry = 0
    for (const da of ad) {
      const partial = da * db + carry
      const nextCarry = Math.floor(partial / 10)
      if (nextCarry > 0) carries++
      carry = nextCarry
    }
    if (carry > 0) carries++
  }
  return carries
}

function countDivisionRemainderSteps(dividend: number, divisor: number): number {
  if (divisor <= 0) return 0
  const text = Math.abs(Math.trunc(dividend)).toString()
  let current = 0
  let steps = 0
  let started = false
  for (const ch of text) {
    current = current * 10 + Number(ch)
    if (!started && current < divisor) continue
    started = true
    if (current % divisor !== 0) steps++
    current %= divisor
  }
  return steps
}

function carryCount(op: 'ADD' | 'SUBTRACT' | 'MULTIPLY' | 'DIVIDE', a: number, b: number): number {
  if (op === 'ADD') return countAdditionCarries(a, b)
  if (op === 'SUBTRACT') return countSubtractionBorrows(Math.max(a, b), Math.min(a, b))
  if (op === 'MULTIPLY') return countMultiplicationCarries(a, b)
  return countDivisionRemainderSteps(a, Math.max(1, b))
}

/**
 * Groen & Parkman-inspired arithmetic complexity.
 * Formula: Cb = (digits(operands) * operationWeight) + (carryCount * 2)
 */
function rawComplexity(op: 'ADD' | 'SUBTRACT' | 'MULTIPLY' | 'DIVIDE', a: number, b: number): number {
  const digitsTerm = chunkLoadForOperands(a, b) * operationWeight(op)
  const carryTerm = carryCount(op, a, b) * 2
  return digitsTerm + carryTerm
}

/**
 * Affine normalization to 0-100 anchored by canonical examples:
 * - "2 + 2" => 0
 * - "(998 * 76) / 4" => 100
 */
function normalizeRawComplexity(raw: number): number {
  const normalized = ((raw - RAW_SCORE_MIN) / (RAW_SCORE_MAX - RAW_SCORE_MIN)) * 100
  return Math.max(0, Math.min(100, normalized))
}

function normalizeTargetComplexityInput(targetScore: number): number {
  const clamped = clampScore(targetScore)
  return (1 - clamped) * 100
}

/**
 * Estimation complexity model (psychophysics-inspired).
 * Formula: Cb_raw = (1 / toleranceFactor) * categoryWeight
 *
 * A stricter tolerated error (smaller tolerance factor) increases decision load.
 * Category weights approximate semantic + quantitative effort by prompt family.
 */
function estimationRawComplexity(toleranceFactor: number, categoryWeight: number): number {
  const safeTolerance = Math.max(ESTIMATION_TOLERANCE_MIN, toleranceFactor)
  return (1 / safeTolerance) * categoryWeight
}

/**
 * Affine normalization to [0, 100] for estimation complexity.
 * Anchors:
 * - 50% tolerance in SIMPLE category -> near 0
 * - 5% tolerance in BUDGET category -> near 100
 */
function normalizeEstimationComplexity(raw: number): number {
  return normalizeRawTo100(raw, ESTIMATION_RAW_MIN, ESTIMATION_RAW_MAX)
}

function estimationComplexityScore(toleranceFactor: number, category: keyof typeof ESTIMATION_CATEGORY_WEIGHTS): number {
  const raw = estimationRawComplexity(toleranceFactor, ESTIMATION_CATEGORY_WEIGHTS[category])
  return normalizeEstimationComplexity(raw)
}

function scrambleLetters(answer: string): string[] {
  const letters = answer.toLowerCase().split('')
  for (let i = letters.length - 1; i > 0; i--) {
    const j = randInt(0, i)
    ;[letters[i], letters[j]] = [letters[j], letters[i]]
  }
  if (letters.join('') === answer.toLowerCase() && letters.length > 1) {
    ;[letters[0], letters[1]] = [letters[1], letters[0]]
  }
  return letters
}

function hardBandFilter(targetScore: number, word: RankedWord): boolean {
  if (targetScore <= 0.4) return word.word.length >= 6 && word.frequencyIndex > 1000
  if (targetScore >= 0.7) return word.word.length <= 4 && word.frequencyIndex <= 1000
  return true
}

interface SumPairCandidate {
  staticNumbers: number[]
  pairsPerRound: number
  minValue: number
  maxValue: number
}

interface ImagePairCandidate {
  pairCount: number
  maxPairsPerBackground: number
  colorCount: number
}

const SUM_PAIR_CANDIDATES: SumPairCandidate[] = (() => {
  const staticOptions = [[2], [3], [5], [7], [2, 5], [3, 5], [5, 7], [2, 5, 10], [3, 7, 10]]
  const maxValues = [25, 35, 45, 55, 70, 85, 99]
  const out: SumPairCandidate[] = []
  for (const staticNumbers of staticOptions) {
    for (let pairsPerRound = 3; pairsPerRound <= 6; pairsPerRound++) {
      for (const maxValue of maxValues) {
        out.push({ staticNumbers, pairsPerRound, minValue: 1, maxValue })
      }
    }
  }
  return out
})()

const IMAGE_PAIR_CANDIDATES: ImagePairCandidate[] = (() => {
  const out: ImagePairCandidate[] = []
  for (let pairCount = 3; pairCount <= 8; pairCount++) {
    for (let maxPairsPerBackground = 1; maxPairsPerBackground <= 3; maxPairsPerBackground++) {
      for (let colorCount = 2; colorCount <= 5; colorCount++) {
        out.push({ pairCount, maxPairsPerBackground, colorCount })
      }
    }
  }
  return out
})()

/**
 * Fitts-like spatial load for pair-matching boards.
 * Formula: Cb = pairCount * (1 + (varietyFactor / 10)).
 *
 * pairCount captures visual search span; varietyFactor captures choice entropy
 * (Hick-Hyman compatible with larger discriminative sets).
 */
function basicPairRawComplexity(pairCount: number, varietyFactor: number): number {
  return pairCount * (1 + varietyFactor / 10)
}

function normalizeRawTo100(raw: number, minRaw: number, maxRaw: number): number {
  if (maxRaw <= minRaw) return 0
  const normalized = ((raw - minRaw) / (maxRaw - minRaw)) * 100
  return Math.max(0, Math.min(100, normalized))
}

/**
 * Information-theory N-back load estimate.
 * Formula: Cb_raw = n * streamCount * log2(stimulusVariety)
 *
 * Scientific basis:
 * - `log2(stimulusVariety)` follows Shannon information bits per stimulus.
 * - Multiplication by `n` models temporal span pressure.
 * - Multiplication by `streamCount` captures dual-task interference.
 *
 * Normalization anchors:
 * - single 1-back with 2 stimuli -> 0
 * - dual 4-back with 4 stimuli per stream -> 100
 */
export function estimateNBackComplexityScore(
  n: number,
  streamCount: number,
  stimulusVariety: number,
): number {
  const safeN = Math.max(1, Math.trunc(n))
  const safeStreams = Math.max(1, Math.trunc(streamCount))
  const safeVariety = Math.max(2, Math.trunc(stimulusVariety))
  const raw = safeN * safeStreams * Math.log2(safeVariety)
  return normalizeRawTo100(raw, NBACK_RAW_MIN, NBACK_RAW_MAX)
}

/**
 * Digit Span load estimate based on Miller's memory-capacity framing.
 * Formula: Cb = length
 *
 * Normalization anchors:
 * - length 3 -> 0
 * - length 10 -> 100
 */
export function estimateDigitSpanComplexityScore(length: number): number {
  const safeLength = Math.max(3, Math.min(10, Math.trunc(length)))
  return normalizeRawTo100(safeLength, 3, 10)
}

/**
 * Miller + Fitts inspired MEMORY_CARD_PAIRS complexity.
 * Formula: Cb = pairCount * (1 + (varietyFactor / 10)),
 * varietyFactor = pairCount / 2.
 *
 * Normalization anchors use practical mobile grid bounds (2..8 pairs).
 */
export function estimateMemoryCardComplexityScore(pairCount: number): number {
  const varietyFactor = pairCount / 2
  const raw = basicPairRawComplexity(pairCount, varietyFactor)
  const minRaw = basicPairRawComplexity(2, 1)
  const maxRaw = basicPairRawComplexity(10, 5)
  return normalizeRawTo100(raw, minRaw, maxRaw)
}

/**
 * Fitts + spatial-color load complexity for IMAGE_PAIR.
 * Formula: Cb = pairCount * (1 + (varietyFactor / 10)),
 * varietyFactor = colorCount + maxPairsPerBackground.
 *
 * Normalization anchors use min/max practical board sizes (3..8 pairs).
 */
export function estimateImagePairComplexityScore(params: ImagePairParamsDto): number {
  const varietyFactor = params.colorCount + params.maxPairsPerBackground
  const raw = basicPairRawComplexity(params.pairCount, varietyFactor)
  const minRaw = basicPairRawComplexity(3, 4)
  const maxRaw = basicPairRawComplexity(8, 6)
  return normalizeRawTo100(raw, minRaw, maxRaw)
}

/**
 * Fitts + arithmetic spread complexity for SUM_PAIR.
 * Base formula: Cb = pairCount * (1 + (varietyFactor / 10))
 * with pairCount = staticNumbers.length * pairsPerRound.
 *
 * Added variance factor for math spacing:
 * Csum = Cb * (1 + (maxValue - minValue) / 100).
 *
 * Final score is normalized to 0..100 with min/max screen-feasible grid loads.
 */
export function estimateSumPairComplexityScore(params: SumPairParamsDto): number {
  const staticCount = Math.max(1, params.staticNumbers.length)
  const pairCount = Math.max(1, staticCount * params.pairsPerRound)
  const varietyFactor = staticCount * 2
  const minValue = params.minValue ?? 1
  const maxValue = params.maxValue ?? 99
  const variance = Math.max(0, maxValue - minValue)
  const raw = basicPairRawComplexity(pairCount, varietyFactor) * (1 + variance / 100)
  const minRaw = basicPairRawComplexity(3, 2) * (1 + 10 / 100)
  const maxRaw = basicPairRawComplexity(18, 6) * (1 + 98 / 100)
  return normalizeRawTo100(raw, minRaw, maxRaw)
}

function easeScoreFromComplexityScore(complexityScore: number): number {
  return Math.max(0, Math.min(1, 1 - complexityScore / 100))
}

class SumPairParamGenerator implements ExerciseParamGenerator<Extract<ExerciseParams, { type: 'SUM_PAIR' }>> {
  readonly type = 'SUM_PAIR' as const

  generateParamsFromScore(targetScore: number): Extract<ExerciseParams, { type: 'SUM_PAIR' }> {
    const targetComplexity = normalizeTargetComplexityInput(targetScore)
    let best = SUM_PAIR_CANDIDATES[0]
    let bestError = Number.POSITIVE_INFINITY
    for (const candidate of SUM_PAIR_CANDIDATES) {
      const estimated = estimateSumPairComplexityScore(candidate)
      const error = Math.abs(estimated - targetComplexity)
      if (error < bestError) {
        best = candidate
        bestError = error
      }
      if (error <= COMPLEXITY_TOLERANCE_POINTS) break
    }

    return {
      type: 'SUM_PAIR',
      params: {
        staticNumbers: best.staticNumbers,
        pairsPerRound: best.pairsPerRound,
        minValue: best.minValue,
        maxValue: best.maxValue,
      },
    }
  }
}

class MemoryCardParamGenerator implements ExerciseParamGenerator<Extract<ExerciseParams, { type: 'MEMORY_CARD_PAIRS' }>> {
  readonly type = 'MEMORY_CARD_PAIRS' as const

  generateParamsFromScore(targetScore: number): Extract<ExerciseParams, { type: 'MEMORY_CARD_PAIRS' }> {
    const targetComplexity = normalizeTargetComplexityInput(targetScore)
    let pairCount = 3
    let bestError = Number.POSITIVE_INFINITY
    for (let candidate = 2; candidate <= 10; candidate++) {
      const complexity = estimateMemoryCardComplexityScore(candidate)
      const error = Math.abs(complexity - targetComplexity)
      if (error < bestError) {
        pairCount = candidate
        bestError = error
      }
      if (error <= COMPLEXITY_TOLERANCE_POINTS) break
    }
    return {
      type: 'MEMORY_CARD_PAIRS',
      params: {
        pairCount,
        symbols: [],
        shuffledDeck: [],
      },
    }
  }
}

class ImagePairParamGenerator implements ExerciseParamGenerator<Extract<ExerciseParams, { type: 'IMAGE_PAIR' }>> {
  readonly type = 'IMAGE_PAIR' as const

  generateParamsFromScore(targetScore: number): Extract<ExerciseParams, { type: 'IMAGE_PAIR' }> {
    const targetComplexity = normalizeTargetComplexityInput(targetScore)
    let best = IMAGE_PAIR_CANDIDATES[0]
    let bestError = Number.POSITIVE_INFINITY
    for (const candidate of IMAGE_PAIR_CANDIDATES) {
      const complexity = estimateImagePairComplexityScore(candidate)
      const error = Math.abs(complexity - targetComplexity)
      if (error < bestError) {
        best = candidate
        bestError = error
      }
      if (error <= COMPLEXITY_TOLERANCE_POINTS) break
    }
    return {
      type: 'IMAGE_PAIR',
      params: {
        pairCount: best.pairCount,
        maxPairsPerBackground: best.maxPairsPerBackground,
        colorCount: best.colorCount,
      },
    }
  }
}

class FlashcardParamGenerator implements ExerciseParamGenerator<Extract<ExerciseParams, { type: 'FLASHCARD_QA' }>> {
  readonly type = 'FLASHCARD_QA' as const

  generateParamsFromScore(targetScore: number): Extract<ExerciseParams, { type: 'FLASHCARD_QA' }> {
    const target = normalizeTargetComplexityInput(targetScore)
    let best: Extract<ExerciseParams, { type: 'FLASHCARD_QA' }> = {
      type: 'FLASHCARD_QA',
      params: {
        operation: 'ADD',
        firstOperand: 2,
        secondOperand: 2,
        complexityScore: 0,
      },
    }
    let bestError = Number.POSITIVE_INFINITY

    let attempts = 0
    while (attempts < SCORE_MAX_ATTEMPTS) {
      const op = (['ADD', 'SUBTRACT', 'MULTIPLY', 'DIVIDE'] as const)[randInt(0, 3)]
      const firstOperand = randInt(2, 999)
      const secondOperand = op === 'DIVIDE' ? randInt(2, 99) : randInt(2, 999)
      const raw = rawComplexity(op, firstOperand, secondOperand)
      const complexityScore = normalizeRawComplexity(raw)
      const error = Math.abs(complexityScore - target)
      if (error < bestError) {
        bestError = error
        best = {
          type: 'FLASHCARD_QA',
          params: {
            operation: op,
            firstOperand,
            secondOperand,
            complexityScore,
          },
        }
      }
      if (error <= COMPLEXITY_TOLERANCE_POINTS) {
        return {
          type: 'FLASHCARD_QA',
          params: {
            operation: op,
            firstOperand,
            secondOperand,
            complexityScore,
          },
        }
      }
      attempts++
    }

    return best
  }
}

class MathChainParamGenerator implements ExerciseParamGenerator<Extract<ExerciseParams, { type: 'MATH_CHAIN' }>> {
  readonly type = 'MATH_CHAIN' as const

  generateParamsFromScore(targetScore: number): Extract<ExerciseParams, { type: 'MATH_CHAIN' }> {
    const target = normalizeTargetComplexityInput(targetScore)
    let best: Extract<ExerciseParams, { type: 'MATH_CHAIN' }> = {
      type: 'MATH_CHAIN',
      params: {
        startNumber: 2,
        steps: [{ operation: 'ADD', operand: 2 }],
        complexityScore: 0,
      },
    }
    let bestError = Number.POSITIVE_INFINITY

    let attempts = 0
    while (attempts < SCORE_MAX_ATTEMPTS) {
      const stepsCount = randInt(2, 4)
      const steps: Array<{ operation: 'ADD' | 'SUBTRACT' | 'MULTIPLY' | 'DIVIDE'; operand: number }> = []
      const startNumber = randInt(10, 999)
      let running = startNumber
      let rawTotal = 0

      for (let i = 0; i < stepsCount; i++) {
        const op = (['ADD', 'SUBTRACT', 'MULTIPLY', 'DIVIDE'] as const)[randInt(0, 3)]
        const operand = op === 'DIVIDE' ? randInt(2, 9) : randInt(2, 99)
        rawTotal += rawComplexity(op, Math.max(1, running), operand)
        if (op === 'ADD') running = running + operand
        else if (op === 'SUBTRACT') running = Math.max(1, running - operand)
        else if (op === 'MULTIPLY') running = running * operand
        else running = Math.max(1, Math.floor(running / operand))
        steps.push({ operation: op, operand })
      }

      const complexityScore = normalizeRawComplexity(rawTotal)
      const error = Math.abs(complexityScore - target)
      if (error < bestError) {
        bestError = error
        best = {
          type: 'MATH_CHAIN',
          params: {
            startNumber,
            steps,
            complexityScore,
          },
        }
      }
      if (error <= COMPLEXITY_TOLERANCE_POINTS) {
        return {
          type: 'MATH_CHAIN',
          params: {
            startNumber,
            steps,
            complexityScore,
          },
        }
      }
      attempts++
    }

    return {
      ...best,
    }
  }
}

class WordleParamGenerator implements ExerciseParamGenerator<Extract<ExerciseParams, { type: 'WORDLE' }>> {
  readonly type = 'WORDLE' as const

  generateParamsFromScore(targetScore: number): Extract<ExerciseParams, { type: 'WORDLE' }> {
    const normalizedTarget = clampScore(targetScore)
    const targetComplexity = normalizeTargetComplexityInput(normalizedTarget)
    const lexicon = lexiconForLanguage('fr')
    const attemptsSpace = [8, 7, 6, 5, 4, 3]
    let bestWord = lexicon[0]
    let bestAttempts = 6
    let bestDiff = Number.POSITIVE_INFINITY
    for (const word of lexicon) {
      if (!hardBandFilter(normalizedTarget, word)) continue
      if (word.word.length < 3 || word.word.length > 7) continue
      for (const maxAttempts of attemptsSpace) {
        if (normalizedTarget <= 0.4 && maxAttempts > 5) continue
        if (normalizedTarget >= 0.7 && maxAttempts < 6) continue
        const complexity = wordleComplexityScore(word.word, maxAttempts)
        const diff = Math.abs(complexity - targetComplexity)
        if (diff < bestDiff) {
          bestDiff = diff
          bestWord = word
          bestAttempts = maxAttempts
        }
      }
    }
    return {
      type: 'WORDLE',
      params: {
        answer: bestWord.word,
        wordLength: bestWord.word.length,
        maxAttempts: bestAttempts,
        language: 'fr',
      },
    }
  }
}

class AnagramParamGenerator implements ExerciseParamGenerator<Extract<ExerciseParams, { type: 'ANAGRAM' }>> {
  readonly type = 'ANAGRAM' as const

  generateParamsFromScore(targetScore: number): Extract<ExerciseParams, { type: 'ANAGRAM' }> {
    const normalizedTarget = clampScore(targetScore)
    const targetComplexity = normalizeTargetComplexityInput(normalizedTarget)
    const lexicon = lexiconForLanguage('fr')
    let bestWord = lexicon[0]
    let bestDiff = Number.POSITIVE_INFINITY
    for (const word of lexicon) {
      if (!hardBandFilter(normalizedTarget, word)) continue
      if (word.word.length < 3 || word.word.length > 7) continue
      const complexity = anagramComplexityScore(word.word.length, word.frequencyIndex)
      const diff = Math.abs(complexity - targetComplexity)
      if (diff < bestDiff) {
        bestDiff = diff
        bestWord = word
      }
    }

    const hintIntervalSeconds = normalizedTarget >= 0.7 ? 7 : normalizedTarget <= 0.4 ? 15 : 10
    return {
      type: 'ANAGRAM',
      params: {
        answer: bestWord.word,
        scrambledLetters: scrambleLetters(bestWord.word),
        hintIntervalSeconds,
        letterColorHint: normalizedTarget >= 0.5,
      },
    }
  }
}

class NBackParamGenerator implements ExerciseParamGenerator<Extract<ExerciseParams, { type: 'N_BACK' }>> {
  readonly type = 'N_BACK' as const

  generateParamsFromScore(targetScore: number): Extract<ExerciseParams, { type: 'N_BACK' }> {
    const targetComplexity = normalizeTargetComplexityInput(targetScore)
    let best = {
      n: 1,
      suitCount: 2,
      complexityScore: estimateNBackComplexityScore(1, 1, 2),
    }
    let bestError = Number.POSITIVE_INFINITY

    for (let n = 1; n <= 6; n++) {
      for (const suitCount of [2, 3, 4]) {
        const complexityScore = estimateNBackComplexityScore(n, 1, suitCount)
        const error = Math.abs(complexityScore - targetComplexity)
        if (error < bestError) {
          bestError = error
          best = { n, suitCount, complexityScore }
        }
      }
    }

    return { type: 'N_BACK', params: best }
  }
}

class DualNBackParamGenerator implements ExerciseParamGenerator<Extract<ExerciseParams, { type: 'DUAL_NBACK' }>> {
  readonly type = 'DUAL_NBACK' as const

  generateParamsFromScore(targetScore: number): Extract<ExerciseParams, { type: 'DUAL_NBACK' }> {
    const targetComplexity = normalizeTargetComplexityInput(targetScore)
    let best = {
      n: 1,
      gridSize: 2,
      colorCount: 2,
      complexityScore: estimateNBackComplexityScore(1, 2, 2),
    }
    let bestError = Number.POSITIVE_INFINITY

    for (let n = 1; n <= 4; n++) {
      for (const gridSize of [2, 3]) {
        for (const colorCount of [2, 3, 4]) {
          // Priority policy: n first, then dual stream mode (fixed here), then variety.
          const variety = Math.min(gridSize * gridSize, colorCount)
          const complexityScore = estimateNBackComplexityScore(n, 2, variety)
          const error = Math.abs(complexityScore - targetComplexity)
          if (error < bestError) {
            bestError = error
            best = { n, gridSize, colorCount, complexityScore }
          }
        }
      }
    }

    return { type: 'DUAL_NBACK', params: best }
  }
}

class DigitSpanParamGenerator implements ExerciseParamGenerator<Extract<ExerciseParams, { type: 'DIGIT_SPAN' }>> {
  readonly type = 'DIGIT_SPAN' as const

  generateParamsFromScore(targetScore: number): Extract<ExerciseParams, { type: 'DIGIT_SPAN' }> {
    const targetComplexity = normalizeTargetComplexityInput(targetScore)
    let bestLength = 3
    let bestComplexity = estimateDigitSpanComplexityScore(bestLength)
    let bestError = Number.POSITIVE_INFINITY

    for (let startLength = 3; startLength <= 10; startLength++) {
      const complexityScore = estimateDigitSpanComplexityScore(startLength)
      const error = Math.abs(complexityScore - targetComplexity)
      if (error < bestError) {
        bestError = error
        bestLength = startLength
        bestComplexity = complexityScore
      }
    }

    return {
      type: 'DIGIT_SPAN',
      params: {
        startLength: bestLength,
        complexityScore: bestComplexity,
      },
    }
  }
}

class EstimationParamGenerator implements ExerciseParamGenerator<Extract<ExerciseParams, { type: 'ESTIMATION' }>> {
  readonly type = 'ESTIMATION' as const

  generateParamsFromScore(targetScore: number): Extract<ExerciseParams, { type: 'ESTIMATION' }> {
    const targetComplexity = normalizeTargetComplexityInput(targetScore)
    const categories = Object.keys(ESTIMATION_CATEGORY_WEIGHTS) as Array<keyof typeof ESTIMATION_CATEGORY_WEIGHTS>

    let bestCategory: keyof typeof ESTIMATION_CATEGORY_WEIGHTS = 'SIMPLE'
    let bestTolerance = ESTIMATION_TOLERANCE_MAX
    let bestComplexity = estimationComplexityScore(bestTolerance, bestCategory)
    let bestError = Math.abs(bestComplexity - targetComplexity)

    for (const category of categories) {
      for (
        let tolerance = ESTIMATION_TOLERANCE_MIN;
        tolerance <= ESTIMATION_TOLERANCE_MAX;
        tolerance += ESTIMATION_TOLERANCE_STEP
      ) {
        const roundedTolerance = Number(tolerance.toFixed(3))
        const complexityScore = estimationComplexityScore(roundedTolerance, category)
        const error = Math.abs(complexityScore - targetComplexity)
        if (error < bestError) {
          bestError = error
          bestCategory = category
          bestTolerance = roundedTolerance
          bestComplexity = complexityScore
        }
      }
    }

    return {
      type: 'ESTIMATION',
      params: {
        correctAnswer: 100,
        unit: '',
        toleranceFactor: bestTolerance,
        category: bestCategory,
        hint: null,
        timeWeightHigher: bestCategory === 'SIMPLE',
        complexityScore: bestComplexity,
      },
    }
  }
}

const generatorRegistry: Record<SupportedExerciseType, ExerciseParamGenerator<ExerciseParams>> = {
  SUM_PAIR: new SumPairParamGenerator(),
  MEMORY_CARD_PAIRS: new MemoryCardParamGenerator(),
  IMAGE_PAIR: new ImagePairParamGenerator(),
  FLASHCARD_QA: new FlashcardParamGenerator(),
  MATH_CHAIN: new MathChainParamGenerator(),
  WORDLE: new WordleParamGenerator(),
  ANAGRAM: new AnagramParamGenerator(),
  ESTIMATION: new EstimationParamGenerator(),
  N_BACK: new NBackParamGenerator(),
  DUAL_NBACK: new DualNBackParamGenerator(),
  DIGIT_SPAN: new DigitSpanParamGenerator(),
}

export function generateParamsFromScore(
  type: SupportedExerciseType,
  targetScore: number
): ExerciseParams {
  return generatorRegistry[type].generateParamsFromScore(targetScore)
}

export function listExerciseParamGenerators(): SupportedExerciseType[] {
  return Object.keys(generatorRegistry) as SupportedExerciseType[]
}

export function estimateGeneratedScore(params: ExerciseParams): number {
  if (params.type === 'SUM_PAIR') {
    return easeScoreFromComplexityScore(estimateSumPairComplexityScore(params.params))
  }
  if (params.type === 'MEMORY_CARD_PAIRS') {
    return easeScoreFromComplexityScore(estimateMemoryCardComplexityScore(params.params.pairCount))
  }
  if (params.type === 'IMAGE_PAIR') {
    return easeScoreFromComplexityScore(estimateImagePairComplexityScore(params.params))
  }
  if (params.type === 'FLASHCARD_QA') {
    return Math.max(0, Math.min(1, 1 - params.params.complexityScore / 100))
  }
  if (params.type === 'MATH_CHAIN') {
    return Math.max(0, Math.min(1, 1 - params.params.complexityScore / 100))
  }
  if (params.type === 'WORDLE') {
    return Math.max(0, Math.min(1, 1 - wordleComplexityScore(params.params.answer, params.params.maxAttempts ?? 6) / 100))
  }
  if (params.type === 'ESTIMATION') {
    return Math.max(0, Math.min(1, 1 - params.params.complexityScore / 100))
  }
  if (params.type === 'N_BACK' || params.type === 'DUAL_NBACK' || params.type === 'DIGIT_SPAN') {
    return easeScoreFromComplexityScore(params.params.complexityScore)
  }
  const freq = lexiconForLanguage('fr').find((w) => w.word === params.params.answer.toLowerCase())?.frequencyIndex ?? 1500
  return Math.max(0, Math.min(1, 1 - anagramComplexityScore(params.params.answer.length, freq) / 100))
}
