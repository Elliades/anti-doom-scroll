import type {
  DigitSpanParamsDto,
  EstimationParamsDto,
  ExerciseDto,
  ImagePairParamsDto,
  MathChainParamsDto,
  NBackParamsDto,
  NBackGridParamsDto,
  DualNBackCardParamsDto,
  DualNBackGridParamsDto,
  GridStimulusDto,
  SumPairGroupDto,
  SumPairCardDto,
  WordleParamsDto,
  AnagramParamsDto,
} from '../types/api'
import { loadWordListWords } from '../utils/wordleWordLoader'
import { generateParamsFromScore } from './exerciseParamGenerators'

// ---------------------------------------------------------------------------
// Shared helpers
// ---------------------------------------------------------------------------

function shuffled<T>(arr: T[]): T[] {
  const a = [...arr]
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[a[i], a[j]] = [a[j], a[i]]
  }
  return a
}

function pickRandom<T>(arr: T[]): T {
  return arr[Math.floor(Math.random() * arr.length)]
}

function randInt(min: number, max: number): number {
  return min + Math.floor(Math.random() * (max - min + 1))
}

// ---------------------------------------------------------------------------
// Arithmetic complexity model (mirrors ArithmeticComplexity.kt)
// ---------------------------------------------------------------------------

function digits(n: number): number {
  if (n <= 0) return 1
  let x = n
  let d = 0
  while (x > 0) {
    d++
    x = Math.floor(x / 10)
  }
  return d
}

function digitAt(n: number, i: number): number {
  return Math.floor(n / Math.pow(10, i)) % 10
}

function addCarriesAndMaxColumnSum(a: number, b: number): { carries: number; maxConsec: number; maxColSum: number } {
  let totalCarries = 0
  let maxConsecutive = 0
  let currentConsecutive = 0
  let maxColumnSum = 0
  let carry = 0
  let pa = a
  let pb = b
  while (pa > 0 || pb > 0 || carry > 0) {
    const da = pa % 10
    const db = pb % 10
    const colSum = da + db + carry
    maxColumnSum = Math.max(maxColumnSum, colSum)
    if (colSum >= 10) {
      totalCarries++
      currentConsecutive++
      carry = 1
    } else {
      maxConsecutive = Math.max(maxConsecutive, currentConsecutive)
      currentConsecutive = 0
      carry = 0
    }
    pa = Math.floor(pa / 10)
    pb = Math.floor(pb / 10)
  }
  maxConsecutive = Math.max(maxConsecutive, currentConsecutive)
  return { carries: totalCarries, maxConsec: maxConsecutive, maxColSum: maxColumnSum }
}

function subtractBorrows(a: number, b: number): { borrows: number; maxConsec: number; zeros: number } {
  let totalBorrows = 0
  let maxConsecutive = 0
  let currentConsecutive = 0
  let zerosInChains = 0
  const len = digits(a)
  let borrow = 0
  for (let i = 0; i < len; i++) {
    const da = digitAt(a, i) - borrow
    const db = digitAt(b, i)
    if (da < db) {
      totalBorrows++
      currentConsecutive++
      if (da === 0) zerosInChains++
      borrow = 1
    } else {
      maxConsecutive = Math.max(maxConsecutive, currentConsecutive)
      currentConsecutive = 0
      borrow = 0
    }
  }
  maxConsecutive = Math.max(maxConsecutive, currentConsecutive)
  return { borrows: totalBorrows, maxConsec: maxConsecutive, zeros: zerosInChains }
}

function complexityAdd(a: number, b: number): number {
  if (a < 0 || b < 0) return 0
  const D = Math.max(digits(a), digits(b))
  const { carries: C, maxConsec: MC, maxColSum: SD } = addCarriesAndMaxColumnSum(a, b)
  const ID = Math.abs(digits(a) - digits(b))
  return 1.0 * D + 2.5 * C + 1.5 * MC + 0.2 * SD + 0.5 * ID
}

function complexitySubtract(a: number, b: number): number {
  if (a < 0 || b < 0 || b > a) return 0
  const D = digits(a)
  const { borrows, maxConsec, zeros } = subtractBorrows(a, b)
  return 1.0 * D + 3.0 * borrows + 2.0 * maxConsec + 1.5 * zeros
}

function complexityMultiply(a: number, b: number): number {
  if (a < 0 || b < 0) return 0
  const d1 = digits(a)
  const d2 = digits(b)
  if (a <= 9 && b <= 9) {
    const H = a >= 7 || b >= 7 ? 1 : 0
    return 2.0 + 1.0 * H
  }
  const sp = d1 * d2
  let totalCarries = 0
  let structuralZeros = 0
  const digitsA: number[] = []
  const digitsB: number[] = []
  let xa = a
  while (xa > 0) { digitsA.push(xa % 10); xa = Math.floor(xa / 10) }
  let xb = b
  while (xb > 0) { digitsB.push(xb % 10); xb = Math.floor(xb / 10) }
  for (const db of digitsB) {
    let carry = 0
    for (const da of digitsA) {
      const step = da * db + carry
      if (step >= 10) { totalCarries++; carry = Math.floor(step / 10) } else { carry = 0 }
    }
    if (carry > 0) totalCarries++
  }
  let product = a * b
  while (product > 0) {
    if (product % 10 === 0) structuralZeros++
    product = Math.floor(product / 10)
  }
  return 1.2 * (d1 + d2) + 0.8 * sp + 2.0 * totalCarries + 0.5 * structuralZeros
}

function complexityDivide(dividend: number, divisor: number): number {
  if (divisor <= 0 || dividend < 0) return 0
  const quotient = Math.floor(dividend / divisor)
  if (quotient <= 0) return 0
  const remainder = dividend % divisor
  const D1 = digits(dividend)
  const D2 = digits(divisor)
  const S = digits(quotient)
  const R = remainder !== 0 ? 1 : 0
  const DEC = remainder !== 0 ? 1 : 0
  return 1.5 * D1 + 2.0 * D2 + 2.0 * S + 3.0 * R + 4.0 * DEC
}

function scoreBandFor(difficulty: string): [number, number] {
  switch (difficulty) {
    case 'ULTRA_EASY': return [0.0, 5.0]
    case 'EASY': return [5.0, 15.0]
    case 'MEDIUM': return [15.0, 30.0]
    case 'HARD': return [30.0, 60.0]
    case 'VERY_HARD': return [30.0, 60.0]
    default: return [0.0, 5.0]
  }
}

// ---------------------------------------------------------------------------
// Math Flashcard Generator (mirrors MathFlashcardGenerator.kt)
// ---------------------------------------------------------------------------

interface MathGenResult {
  prompt: string
  expectedAnswer: string
  complexityScore: number
}

function generateMathAdd(firstMin: number, firstMax: number, secondMin: number, secondMax: number): [string, string] {
  const a = randInt(firstMin, firstMax)
  const b = randInt(secondMin, secondMax)
  return [`What is ${a} + ${b}?`, String(a + b)]
}

function generateMathSubtract(firstMin: number, firstMax: number, secondMin: number, secondMax: number): [string, string] {
  const a = randInt(firstMin, firstMax)
  const bMax = Math.min(a, secondMax)
  const b = randInt(secondMin, Math.max(bMax, secondMin))
  return [`What is ${a} \u2212 ${b}?`, String(a - b)]
}

function generateMathMultiply(firstMin: number, firstMax: number, secondMin: number, secondMax: number): [string, string] {
  const a = randInt(firstMin, firstMax)
  const b = randInt(secondMin, secondMax)
  return [`What is ${a} \u00d7 ${b}?`, String(a * b)]
}

function generateMathDivide(firstMin: number, firstMax: number, secondMin: number, secondMax: number): [string, string] {
  const divisor = randInt(secondMin, Math.max(secondMin, secondMax))
  const quotient = randInt(firstMin, firstMax)
  const dividend = divisor * quotient
  return [`What is ${dividend} \u00f7 ${divisor}?`, String(quotient)]
}

function symbolForOperation(op: string): string {
  if (op === 'ADD') return '+'
  if (op === 'SUBTRACT') return '\u2212'
  if (op === 'MULTIPLY') return '\u00d7'
  return '\u00f7'
}

function buildFlashcardFromGeneratedParams(
  operation: string,
  firstOperand: number,
  secondOperand: number,
): { prompt: string; expectedAnswer: string; complexityScore: number } {
  const symbol = symbolForOperation(operation)
  if (operation === 'DIVIDE') {
    const divisor = Math.max(1, secondOperand)
    const quotient = Math.max(1, Math.floor(firstOperand / divisor))
    const dividend = divisor * quotient
    const prompt = `What is ${dividend} ${symbol} ${divisor}?`
    const complexityScore = complexityForOp(operation, prompt, String(quotient))
    return { prompt, expectedAnswer: String(quotient), complexityScore }
  }
  const prompt = `What is ${firstOperand} ${symbol} ${secondOperand}?`
  const answer = operation === 'ADD'
    ? firstOperand + secondOperand
    : operation === 'SUBTRACT'
      ? firstOperand - secondOperand
      : firstOperand * secondOperand
  const complexityScore = complexityForOp(operation, prompt, String(answer))
  return { prompt, expectedAnswer: String(answer), complexityScore }
}

function complexityForOp(op: string, prompt: string, answer: string): number {
  const q = prompt.replace(/^What is /, '').replace(/\?$/, '')
  let parts: string[]
  let a: number, b: number
  switch (op) {
    case 'ADD':
      parts = q.split(' + ')
      if (parts.length !== 2) return 0
      a = parseInt(parts[0]); b = parseInt(parts[1])
      return isNaN(a) || isNaN(b) ? 0 : complexityAdd(a, b)
    case 'SUBTRACT':
      parts = q.split(' \u2212 ')
      if (parts.length !== 2) return 0
      a = parseInt(parts[0]); b = parseInt(parts[1])
      return isNaN(a) || isNaN(b) ? 0 : complexitySubtract(a, b)
    case 'MULTIPLY':
      parts = q.split(' \u00d7 ')
      if (parts.length !== 2) return 0
      a = parseInt(parts[0]); b = parseInt(parts[1])
      return isNaN(a) || isNaN(b) ? 0 : complexityMultiply(a, b)
    case 'DIVIDE':
      parts = q.split(' \u00f7 ')
      if (parts.length !== 2) return 0
      a = parseInt(parts[0]); b = parseInt(parts[1])
      return isNaN(a) || isNaN(b) ? 0 : complexityDivide(parseInt(answer) * b, b)
    default: return 0
  }
}

function operandRangesForDifficulty(difficulty: string): { cap: number } {
  switch (difficulty) {
    case 'ULTRA_EASY': return { cap: 9 }
    case 'EASY': return { cap: 20 }
    case 'MEDIUM': return { cap: 30 }
    case 'HARD':
    case 'VERY_HARD': return { cap: 99 }
    default: return { cap: 20 }
  }
}

export function generateMathFlashcard(op: string, difficulty: string): MathGenResult {
  const { cap } = operandRangesForDifficulty(difficulty)
  const firstMin = 1
  const firstMax = cap
  const secondMin = 1
  const secondMax = cap
  const [minScore, maxScore] = scoreBandFor(difficulty)

  const genFn = op === 'ADD' ? generateMathAdd
    : op === 'SUBTRACT' ? generateMathSubtract
    : op === 'MULTIPLY' ? generateMathMultiply
    : generateMathDivide

  for (let i = 0; i < 80; i++) {
    const [prompt, answer] = genFn(firstMin, firstMax, secondMin, secondMax)
    const score = complexityForOp(op, prompt, answer)
    if (score >= minScore && score <= maxScore) {
      return { prompt, expectedAnswer: answer, complexityScore: score }
    }
  }
  const [prompt, answer] = genFn(firstMin, firstMax, secondMin, secondMax)
  const score = complexityForOp(op, prompt, answer)
  return { prompt, expectedAnswer: answer, complexityScore: score }
}

// ---------------------------------------------------------------------------
// Wordle Generator (mirrors WordleGenerator.kt)
// ---------------------------------------------------------------------------

function normalizeWordleWord(raw: string): string {
  const nfd = raw.normalize('NFD')
  const noMarks = nfd.replace(/\p{M}+/gu, '')
  return noMarks.toLowerCase().replace(/œ/g, 'oe').replace(/æ/g, 'ae')
}

function wordLengthForDifficulty(difficulty: string): number {
  switch (difficulty) {
    case 'EASY':
    case 'ULTRA_EASY': return 3
    case 'MEDIUM': return 5
    case 'HARD': return 6
    case 'VERY_HARD': return 7
    default: return 3
  }
}

export async function generateWordleAnswer(
  language: string,
  wordLength: number,
  _difficulty?: string,
): Promise<string | null> {
  const resolvedLen = wordLength > 0 ? wordLength : (_difficulty ? wordLengthForDifficulty(_difficulty) : 5)
  const words = await loadWordListWords(language, resolvedLen)
  if (words.length === 0) return null
  return normalizeWordleWord(pickRandom(words))
}

// ---------------------------------------------------------------------------
// N-Back Sequence Generator (mirrors NBackSequenceGenerator.kt)
// ---------------------------------------------------------------------------

const NBACK_SUITS = 'CDHS'
const NBACK_RANKS = ['A', '2', '3', '4', '5', '6', '7', '8', '9', '10', 'J', 'Q', 'K']

export function generateNBackSequence(
  n: number,
  suitCount: number,
  sequenceLength = 12,
): { sequence: string[]; matchIndices: number[] } {
  const suitsToUse = NBACK_SUITS.slice(0, suitCount)
  const pool: string[] = []
  for (const rank of NBACK_RANKS) {
    for (const suit of suitsToUse) {
      pool.push(`${rank}${suit}`)
    }
  }
  const shuffledPool = shuffled(pool)

  const sequence: string[] = []
  const matchIndices: number[] = []

  for (let i = 0; i < sequenceLength; i++) {
    if (i < n) {
      sequence.push(pickRandom(shuffledPool))
    } else {
      const prevCard = sequence[i - n]
      if (Math.random() < 0.35 && shuffledPool.length > 1) {
        sequence.push(prevCard)
        matchIndices.push(i)
      } else {
        const other = shuffledPool.filter((c) => c !== prevCard)
        sequence.push(other.length > 0 ? pickRandom(other) : pickRandom(shuffledPool))
      }
    }
  }

  return { sequence, matchIndices }
}

export function inferSuitCount(sequence: string[]): number {
  const suits = new Set(sequence.map((s) => s.slice(-1)))
  return Math.max(1, Math.min(4, suits.size))
}

// ---------------------------------------------------------------------------
// N-Back Grid Sequence Generator (mirrors NBackGridSequenceGenerator.kt)
// ---------------------------------------------------------------------------

export function generateNBackGridSequence(
  n: number,
  gridSize: number,
  sequenceLength = 12,
): { sequence: number[]; matchIndices: number[] } {
  const cellCount = gridSize * gridSize
  const sequence: number[] = []
  const matchIndices: number[] = []

  for (let i = 0; i < sequenceLength; i++) {
    if (i < n) {
      sequence.push(Math.floor(Math.random() * cellCount))
    } else {
      const prevCell = sequence[i - n]
      if (Math.random() < 0.35) {
        sequence.push(prevCell)
        matchIndices.push(i)
      } else {
        const candidates = Array.from({ length: cellCount }, (_, k) => k).filter((c) => c !== prevCell)
        sequence.push(candidates.length > 0 ? pickRandom(candidates) : Math.floor(Math.random() * cellCount))
      }
    }
  }

  return { sequence, matchIndices }
}

// ---------------------------------------------------------------------------
// Dual N-Back Card Sequence Generator (mirrors DualNBackCardSequenceGenerator.kt)
// ---------------------------------------------------------------------------

const DUAL_SUITS = ['C', 'D', 'H', 'S']
const DUAL_RANKS = NBACK_RANKS

export function generateDualNBackCardSequence(
  n: number,
  suitCount: number,
  sequenceLength = 12,
): { sequence: string[]; matchColorIndices: number[]; matchNumberIndices: number[] } {
  const suits = DUAL_SUITS.slice(0, suitCount)
  const rankSeq: string[] = []
  const suitSeq: string[] = []
  const matchColorIndices: number[] = []
  const matchNumberIndices: number[] = []

  for (let i = 0; i < sequenceLength; i++) {
    if (i < n) {
      rankSeq.push(pickRandom(DUAL_RANKS))
      suitSeq.push(pickRandom(suits))
    } else {
      const prevRank = rankSeq[i - n]
      const prevSuit = suitSeq[i - n]

      if (Math.random() < 0.35) {
        suitSeq.push(prevSuit)
        matchColorIndices.push(i)
      } else {
        const candidates = suits.filter((s) => s !== prevSuit)
        suitSeq.push(candidates.length > 0 ? pickRandom(candidates) : pickRandom(suits))
      }

      if (Math.random() < 0.35) {
        rankSeq.push(prevRank)
        matchNumberIndices.push(i)
      } else {
        const candidates = DUAL_RANKS.filter((r) => r !== prevRank)
        rankSeq.push(candidates.length > 0 ? pickRandom(candidates) : pickRandom(DUAL_RANKS))
      }
    }
  }

  const sequence = rankSeq.map((rank, i) => `${rank}${suitSeq[i]}`)
  return { sequence, matchColorIndices, matchNumberIndices }
}

export function inferDualCardSuitCount(sequence: string[]): number {
  const suits = new Set(sequence.map((s) => s.slice(-1)))
  return Math.max(1, Math.min(4, suits.size))
}

// ---------------------------------------------------------------------------
// Dual N-Back Grid Sequence Generator (mirrors DualNBackGridSequenceGenerator.kt)
// ---------------------------------------------------------------------------

const DEFAULT_GRID_COLORS = ['#4285F4', '#EA4335', '#FBBC04', '#34A853', '#FF6D00', '#9C27B0']

export function generateDualNBackGridSequence(
  n: number,
  gridSize: number,
  colorCount: number,
  sequenceLength = 12,
): {
  sequence: GridStimulusDto[]
  matchPositionIndices: number[]
  matchColorIndices: number[]
  colors: string[]
} {
  const cellCount = gridSize * gridSize
  const colors = DEFAULT_GRID_COLORS.slice(0, colorCount)
  const positions: number[] = []
  const colorSeq: string[] = []
  const matchPositionIndices: number[] = []
  const matchColorIndices: number[] = []

  for (let i = 0; i < sequenceLength; i++) {
    if (i < n) {
      positions.push(Math.floor(Math.random() * cellCount))
      colorSeq.push(pickRandom(colors))
    } else {
      const prevPos = positions[i - n]
      const prevColor = colorSeq[i - n]

      if (Math.random() < 0.35) {
        positions.push(prevPos)
        matchPositionIndices.push(i)
      } else {
        const candidates = Array.from({ length: cellCount }, (_, k) => k).filter((c) => c !== prevPos)
        positions.push(candidates.length > 0 ? pickRandom(candidates) : Math.floor(Math.random() * cellCount))
      }

      if (Math.random() < 0.35) {
        colorSeq.push(prevColor)
        matchColorIndices.push(i)
      } else {
        const candidates = colors.filter((c) => c !== prevColor)
        colorSeq.push(candidates.length > 0 ? pickRandom(candidates) : pickRandom(colors))
      }
    }
  }

  const sequence: GridStimulusDto[] = positions.map((pos, i) => ({
    position: pos,
    color: colorSeq[i],
  }))

  return { sequence, matchPositionIndices, matchColorIndices, colors }
}

// ---------------------------------------------------------------------------
// Memory Card Deck Shuffler (mirrors MemoryCardDeckCache.kt)
// ---------------------------------------------------------------------------

export function shuffleMemoryDeck(symbols: string[]): string[] {
  const pairs = symbols.flatMap((s) => [s, s])
  return shuffled(pairs)
}

// ---------------------------------------------------------------------------
// Anagram Generator (mirrors AnagramGenerator.kt)
// ---------------------------------------------------------------------------

export function scrambleAnagram(answer: string): string[] {
  const letters = [...answer]
  let scrambled = shuffled(letters)
  let attempts = 0
  while (scrambled.join('') === letters.join('') && attempts < 10) {
    scrambled = shuffled(letters)
    attempts++
  }
  return scrambled.map((ch) => ch)
}

// ---------------------------------------------------------------------------
// Sum Pair Generator (mirrors SumPairGenerator.kt)
// ---------------------------------------------------------------------------

const STATIC_COLORS = ['#3b82f6', '#22c55e', '#f59e0b', '#8b5cf6', '#ec4899']

function generateSumPairGroupCards(
  staticVal: number,
  pairCount: number,
  low: number,
  high: number,
): number[] {
  const pool = shuffled(
    Array.from({ length: high - staticVal - low + 1 }, (_, i) => low + i),
  )
  const chosenA: Set<number> = new Set()
  const usedValues: Set<number> = new Set()
  for (const a of pool) {
    if (chosenA.size >= pairCount) break
    if (a + staticVal > high) continue
    if (usedValues.has(a) || usedValues.has(a + staticVal)) continue
    if (chosenA.has(a - 2 * staticVal) || chosenA.has(a + 2 * staticVal)) continue
    chosenA.add(a)
    usedValues.add(a)
    usedValues.add(a + staticVal)
  }
  if (chosenA.size < pairCount) {
    for (const a of pool) {
      if (chosenA.size >= pairCount) break
      if (a + staticVal > high) continue
      if (usedValues.has(a) || usedValues.has(a + staticVal)) continue
      chosenA.add(a)
      usedValues.add(a)
      usedValues.add(a + staticVal)
    }
  }
  const pairs = [...chosenA].flatMap((a) => [a, a + staticVal])
  return shuffled(pairs)
}

export function generateSumPairGroups(
  staticNumbers: number[],
  pairsPerRound: number,
  minValue: number,
  maxValue: number,
): { groups: SumPairGroupDto[]; deck: SumPairCardDto[] } {
  let groups: SumPairGroupDto[]
  if (staticNumbers.length === 1) {
    const k = staticNumbers[0]
    const cards = generateSumPairGroupCards(k, pairsPerRound, minValue, maxValue)
    groups = [{ static: k, color: STATIC_COLORS[0], cards }]
  } else {
    const rangeSize = Math.floor((maxValue - minValue + 1) / staticNumbers.length)
    groups = staticNumbers.map((k, i) => {
      const low = minValue + i * rangeSize
      const high = minValue + (i + 1) * rangeSize - 1
      const cards = generateSumPairGroupCards(k, pairsPerRound, low, high)
      const color = STATIC_COLORS[i % STATIC_COLORS.length]
      return { static: k, color, cards }
    })
  }

  const deck: SumPairCardDto[] = shuffled(
    groups.flatMap((g) =>
      g.cards.map((v) => ({ value: v, static: g.static, color: g.color })),
    ),
  )

  return { groups, deck }
}

// ---------------------------------------------------------------------------
// hydrateExercise — regenerates dynamic content for a frozen ExerciseDto
// ---------------------------------------------------------------------------

export async function hydrateExercise(exercise: ExerciseDto): Promise<ExerciseDto> {
  const type = exercise.type

  switch (type) {
    case 'FLASHCARD_QA':
      return hydrateMathFlashcard(exercise)
    case 'WORDLE':
      return await hydrateWordle(exercise)
    case 'N_BACK':
      return hydrateNBack(exercise)
    case 'N_BACK_GRID':
      return hydrateNBackGrid(exercise)
    case 'DUAL_NBACK_CARD':
      return hydrateDualNBackCard(exercise)
    case 'DUAL_NBACK_GRID':
      return hydrateDualNBackGrid(exercise)
    case 'MEMORY_CARD_PAIRS':
      return hydrateMemoryCard(exercise)
    case 'ANAGRAM':
      return hydrateAnagram(exercise)
    case 'SUM_PAIR':
      return hydrateSumPair(exercise)
    default:
      return exercise
  }
}

function evaluateMathChain(startNumber: number, steps: Array<{ operation: string; operand: number }>): number {
  let value = startNumber
  for (const step of steps) {
    if (step.operation === 'ADD') value += step.operand
    else if (step.operation === 'SUBTRACT') value -= step.operand
    else if (step.operation === 'MULTIPLY') value *= step.operand
    else if (step.operation === 'DIVIDE') value = Math.max(1, Math.floor(value / Math.max(1, step.operand)))
  }
  return value
}

export function applyScoreDrivenParams(exercise: ExerciseDto, targetScore: number): ExerciseDto {
  switch (exercise.type) {
    case 'SUM_PAIR': {
      const generated = generateParamsFromScore('SUM_PAIR', targetScore)
      if (generated.type !== 'SUM_PAIR') return exercise
      return { ...exercise, sumPairParams: generated.params }
    }
    case 'MEMORY_CARD_PAIRS': {
      const generated = generateParamsFromScore('MEMORY_CARD_PAIRS', targetScore)
      if (generated.type !== 'MEMORY_CARD_PAIRS') return exercise
      const symbols = pickDistinctEmojis(generated.params.pairCount)
      return {
        ...exercise,
        memoryCardParams: {
          pairCount: generated.params.pairCount,
          symbols,
          shuffledDeck: shuffleMemoryDeck(symbols),
        },
      }
    }
    case 'IMAGE_PAIR': {
      const generated = generateParamsFromScore('IMAGE_PAIR', targetScore)
      if (generated.type !== 'IMAGE_PAIR') return exercise
      return { ...exercise, imagePairParams: generated.params as ImagePairParamsDto }
    }
    case 'FLASHCARD_QA': {
      const generated = generateParamsFromScore('FLASHCARD_QA', targetScore)
      if (generated.type !== 'FLASHCARD_QA') return exercise
      const built = buildFlashcardFromGeneratedParams(
        generated.params.operation,
        generated.params.firstOperand,
        generated.params.secondOperand,
      )
      return {
        ...exercise,
        mathOperation: generated.params.operation,
        prompt: built.prompt,
        expectedAnswers: [built.expectedAnswer],
        mathComplexityScore: built.complexityScore,
      }
    }
    case 'MATH_CHAIN': {
      const generated = generateParamsFromScore('MATH_CHAIN', targetScore)
      if (generated.type !== 'MATH_CHAIN') return exercise
      const expectedAnswer = evaluateMathChain(generated.params.startNumber, generated.params.steps)
      const params: MathChainParamsDto = {
        startNumber: generated.params.startNumber,
        steps: generated.params.steps.map((s) => ({
          operation: s.operation,
          operand: s.operand,
          complexity: generated.params.complexityScore / Math.max(1, generated.params.steps.length),
        })),
        expectedAnswer,
        totalComplexity: generated.params.complexityScore,
      }
      return { ...exercise, mathChainParams: params, expectedAnswers: [String(expectedAnswer)] }
    }
    case 'WORDLE': {
      const generated = generateParamsFromScore('WORDLE', targetScore)
      if (generated.type !== 'WORDLE') return exercise
      return { ...exercise, wordleParams: generated.params }
    }
    case 'ANAGRAM': {
      const generated = generateParamsFromScore('ANAGRAM', targetScore)
      if (generated.type !== 'ANAGRAM') return exercise
      return { ...exercise, anagramParams: generated.params }
    }
    case 'ESTIMATION': {
      const generated = generateParamsFromScore('ESTIMATION', targetScore)
      if (generated.type !== 'ESTIMATION') return exercise
      const { complexityScore: _complexityScore, ...params } = generated.params
      return { ...exercise, estimationParams: params as EstimationParamsDto }
    }
    case 'DIGIT_SPAN': {
      const generated = generateParamsFromScore('DIGIT_SPAN', targetScore)
      if (generated.type !== 'DIGIT_SPAN') return exercise
      const params: DigitSpanParamsDto = {
        startLength: generated.params.startLength,
        displayTimeMs: Math.max(600, 2200 - generated.params.startLength * 180),
        maxLength: Math.min(14, generated.params.startLength + 4),
      }
      return { ...exercise, digitSpanParams: params }
    }
    case 'N_BACK': {
      const generated = generateParamsFromScore('N_BACK', targetScore)
      if (generated.type !== 'N_BACK') return exercise
      const seqLength = Math.max(12, exercise.nBackParams?.sequence.length ?? exercise.nbackParams?.sequence.length ?? 12)
      const result = generateNBackSequence(generated.params.n, generated.params.suitCount, seqLength)
      const params: NBackParamsDto = { n: generated.params.n, sequence: result.sequence, matchIndices: result.matchIndices }
      return { ...exercise, nBackParams: params, nbackParams: params }
    }
    case 'N_BACK_GRID': {
      const generated = generateParamsFromScore('N_BACK', targetScore)
      if (generated.type !== 'N_BACK') return exercise
      const gridSize = exercise.nBackGridParams?.gridSize ?? exercise.nbackGridParams?.gridSize ?? 3
      const seqLength = Math.max(12, exercise.nBackGridParams?.sequence.length ?? exercise.nbackGridParams?.sequence.length ?? 12)
      const result = generateNBackGridSequence(generated.params.n, gridSize, seqLength)
      const params: NBackGridParamsDto = {
        n: generated.params.n,
        sequence: result.sequence,
        matchIndices: result.matchIndices,
        gridSize,
      }
      return { ...exercise, nBackGridParams: params, nbackGridParams: params }
    }
    case 'DUAL_NBACK_CARD': {
      const generated = generateParamsFromScore('DUAL_NBACK', targetScore)
      if (generated.type !== 'DUAL_NBACK') return exercise
      const seqLength = Math.max(12, exercise.dualNBackCardParams?.sequence.length ?? exercise.dualNbackCardParams?.sequence.length ?? 12)
      const result = generateDualNBackCardSequence(generated.params.n, generated.params.colorCount, seqLength)
      const params: DualNBackCardParamsDto = {
        n: generated.params.n,
        sequence: result.sequence,
        matchColorIndices: result.matchColorIndices,
        matchNumberIndices: result.matchNumberIndices,
      }
      return { ...exercise, dualNBackCardParams: params, dualNbackCardParams: params }
    }
    case 'DUAL_NBACK_GRID': {
      const generated = generateParamsFromScore('DUAL_NBACK', targetScore)
      if (generated.type !== 'DUAL_NBACK') return exercise
      const seqLength = Math.max(12, exercise.dualNBackGridParams?.sequence.length ?? exercise.dualNbackGridParams?.sequence.length ?? 12)
      const result = generateDualNBackGridSequence(
        generated.params.n,
        generated.params.gridSize,
        generated.params.colorCount,
        seqLength,
      )
      const params: DualNBackGridParamsDto = {
        n: generated.params.n,
        sequence: result.sequence,
        matchPositionIndices: result.matchPositionIndices,
        matchColorIndices: result.matchColorIndices,
        colors: result.colors,
        gridSize: generated.params.gridSize,
      }
      return { ...exercise, dualNBackGridParams: params, dualNbackGridParams: params }
    }
    default:
      return exercise
  }
}

function hydrateMathFlashcard(ex: ExerciseDto): ExerciseDto {
  const op = ex.mathOperation
  if (!op) return ex
  const result = generateMathFlashcard(op, ex.difficulty)
  return {
    ...ex,
    prompt: result.prompt,
    expectedAnswers: [result.expectedAnswer],
    mathComplexityScore: result.complexityScore,
  }
}

async function hydrateWordle(ex: ExerciseDto): Promise<ExerciseDto> {
  const wp = ex.wordleParams
  if (!wp) return ex
  const language = wp.language ?? 'fr'
  const answer = await generateWordleAnswer(language, wp.wordLength, ex.difficulty)
  if (!answer) return ex
  const newParams: WordleParamsDto = { ...wp, answer }
  return { ...ex, wordleParams: newParams }
}

function hydrateNBack(ex: ExerciseDto): ExerciseDto {
  const params = ex.nBackParams ?? ex.nbackParams
  if (!params) return ex
  const suitCount = inferSuitCount(params.sequence)
  const seqLen = params.sequence.length
  const result = generateNBackSequence(params.n, suitCount, seqLen)
  const newParams: NBackParamsDto = {
    n: params.n,
    sequence: result.sequence,
    matchIndices: result.matchIndices,
  }
  return { ...ex, nBackParams: newParams, nbackParams: newParams }
}

function hydrateNBackGrid(ex: ExerciseDto): ExerciseDto {
  const params = ex.nBackGridParams ?? ex.nbackGridParams
  if (!params) return ex
  const gridSize = params.gridSize ?? 3
  const seqLen = params.sequence.length
  const result = generateNBackGridSequence(params.n, gridSize, seqLen)
  const newParams: NBackGridParamsDto = {
    n: params.n,
    sequence: result.sequence,
    matchIndices: result.matchIndices,
    gridSize,
  }
  return { ...ex, nBackGridParams: newParams, nbackGridParams: newParams }
}

function hydrateDualNBackCard(ex: ExerciseDto): ExerciseDto {
  const params = ex.dualNBackCardParams ?? ex.dualNbackCardParams
  if (!params) return ex
  const suitCount = inferDualCardSuitCount(params.sequence)
  const seqLen = params.sequence.length
  const result = generateDualNBackCardSequence(params.n, suitCount, seqLen)
  const newParams: DualNBackCardParamsDto = {
    n: params.n,
    sequence: result.sequence,
    matchColorIndices: result.matchColorIndices,
    matchNumberIndices: result.matchNumberIndices,
  }
  return { ...ex, dualNBackCardParams: newParams, dualNbackCardParams: newParams }
}

function hydrateDualNBackGrid(ex: ExerciseDto): ExerciseDto {
  const params = ex.dualNBackGridParams ?? ex.dualNbackGridParams
  if (!params) return ex
  const gridSize = params.gridSize ?? 3
  const colorCount = params.colors?.length ?? 4
  const seqLen = params.sequence.length
  const result = generateDualNBackGridSequence(params.n, gridSize, colorCount, seqLen)
  const newParams: DualNBackGridParamsDto = {
    n: params.n,
    sequence: result.sequence,
    matchPositionIndices: result.matchPositionIndices,
    matchColorIndices: result.matchColorIndices,
    colors: result.colors,
    gridSize,
  }
  return { ...ex, dualNBackGridParams: newParams, dualNbackGridParams: newParams }
}

function hydrateMemoryCard(ex: ExerciseDto): ExerciseDto {
  const params = ex.memoryCardParams
  if (!params) return ex
  const newDeck = shuffleMemoryDeck(params.symbols)
  return {
    ...ex,
    memoryCardParams: { ...params, shuffledDeck: newDeck },
  }
}

function hydrateAnagram(ex: ExerciseDto): ExerciseDto {
  const params = ex.anagramParams
  if (!params) return ex
  const newScrambled = scrambleAnagram(params.answer)
  const newParams: AnagramParamsDto = { ...params, scrambledLetters: newScrambled }
  return { ...ex, anagramParams: newParams }
}

function hydrateSumPair(ex: ExerciseDto): ExerciseDto {
  const params = ex.sumPairParams
  if (!params) return ex
  const minValue = params.minValue ?? 1
  const maxValue = params.maxValue ?? 99
  try {
    const result = generateSumPairGroups(
      params.staticNumbers,
      params.pairsPerRound,
      minValue,
      maxValue,
    )
    return {
      ...ex,
      sumPairGroups: result.groups,
      sumPairDeck: result.deck,
    }
  } catch {
    return ex
  }
}

// ---------------------------------------------------------------------------
// Synthetic pool builders (mirrors LadderExercisePicker buildGenerated*Pool)
// ---------------------------------------------------------------------------

const MEMORY_EMOJI_POOL = [
  '⭐', '❤️', '🔵', '🍎', '🍊', '🍋', '🍇', '🐶', '🐱', '🐰', '🐻', '🦊', '🐼', '🌙', '☀️', '🌧️', '⚡', '🔥', '💧', '🌿',
  '🎵', '🎮', '📚', '✏️', '🎯', '🏀', '⚽', '🎲', '🧩', '🔑', '🧠', '💡', '🌈', '🦋', '🍀', '🌻', '🍄', '🪐', '🎸', '🎹',
  '🚲', '⛵', '🎈', '🎁', '🏠', '🌍', '🧁', '🍪', '🥝', '🫐', '🦆', '🦉', '🐢', '🦀', '🐙', '🦩', '🪁', '🧿', '📌', '🧸',
]

function memoryPairCount(difficulties: string[]): number {
  const set = new Set(difficulties)
  if (set.has('MEDIUM') || set.has('HARD') || set.has('VERY_HARD')) return 6
  if (set.has('EASY') && !set.has('ULTRA_EASY')) return 4
  return 3
}

function pickDistinctEmojis(count: number): string[] {
  return shuffled(MEMORY_EMOJI_POOL).slice(0, Math.min(count, MEMORY_EMOJI_POOL.length))
}

export function buildSyntheticMemoryCardPool(
  difficulties: string[],
  subjectId: string,
  targetScore?: number,
  count = 8,
): ExerciseDto[] {
  const generated = targetScore == null ? null : generateParamsFromScore('MEMORY_CARD_PAIRS', targetScore)
  const pairCount = targetScore == null
    ? memoryPairCount(difficulties)
    : (generated?.type === 'MEMORY_CARD_PAIRS' ? generated.params.pairCount : memoryPairCount(difficulties))
  const difficulty = difficulties[0] ?? 'EASY'
  return Array.from({ length: count }, (_, i) => {
    const symbols = pickDistinctEmojis(pairCount)
    const deck = shuffleMemoryDeck(symbols)
    return {
      id: `mem-gen-offline-${Date.now()}-${i}`,
      subjectId,
      subjectCode: 'MEMORY',
      type: 'MEMORY_CARD_PAIRS',
      difficulty,
      prompt: 'Find all matching pairs. Flip two cards at a time.',
      expectedAnswers: [],
      timeLimitSeconds: 120,
      memoryCardParams: { pairCount, symbols, shuffledDeck: deck },
    } as ExerciseDto
  })
}

function sumPairParamsForDifficulty(difficulties: string[]): {
  statics: number[]
  pairs: number
  minV: number
  maxV: number
} {
  const set = new Set(difficulties)
  if (set.has('MEDIUM') || set.has('HARD') || set.has('VERY_HARD')) {
    const statics = shuffled([2, 5, 10]).slice(0, 2 + Math.floor(Math.random() * 2))
    return { statics, pairs: 4 + Math.floor(Math.random() * 3), minV: 1, maxV: 99 }
  }
  if (set.has('EASY') && !set.has('ULTRA_EASY')) {
    const statics = shuffled([5, 7]).slice(0, 1 + Math.floor(Math.random() * 2))
    return { statics, pairs: 4 + Math.floor(Math.random() * 2), minV: 1, maxV: 50 }
  }
  const statics = shuffled([2, 3, 5]).slice(0, 1)
  return { statics, pairs: 3 + Math.floor(Math.random() * 2), minV: 1, maxV: 30 }
}

export function buildSyntheticSumPairPool(
  difficulties: string[],
  subjectId: string,
  targetScore?: number,
  count = 8,
): ExerciseDto[] {
  const difficulty = difficulties[0] ?? 'EASY'
  return Array.from({ length: count }, (_, i) => {
    const generated = targetScore == null ? null : generateParamsFromScore('SUM_PAIR', targetScore)
    const fromDifficulty = sumPairParamsForDifficulty(difficulties)
    const statics = generated?.type === 'SUM_PAIR' ? generated.params.staticNumbers : fromDifficulty.statics
    const pairs = generated?.type === 'SUM_PAIR' ? generated.params.pairsPerRound : fromDifficulty.pairs
    const minV = generated?.type === 'SUM_PAIR' ? generated.params.minValue ?? 1 : fromDifficulty.minV
    const maxV = generated?.type === 'SUM_PAIR' ? generated.params.maxValue ?? 99 : fromDifficulty.maxV
    let groups: SumPairGroupDto[]
    let deck: SumPairCardDto[]
    try {
      const result = generateSumPairGroups(statics, pairs, minV, maxV)
      groups = result.groups
      deck = result.deck
    } catch {
      return null
    }
    return {
      id: `sumpair-gen-offline-${Date.now()}-${i}`,
      subjectId,
      subjectCode: 'MEMORY',
      type: 'SUM_PAIR',
      difficulty,
      prompt: 'Find pairs where first + static = second.',
      expectedAnswers: [],
      timeLimitSeconds: 120,
      sumPairParams: { staticNumbers: statics, pairsPerRound: pairs, minValue: minV, maxValue: maxV },
      sumPairGroups: groups,
      sumPairDeck: deck,
    } as ExerciseDto
  }).filter((e): e is ExerciseDto => e !== null)
}

export function buildSyntheticMathFlashcardPool(
  difficulties: string[],
  subjectId: string,
  allowedOps?: string[],
  targetScore?: number,
  count = 8,
): ExerciseDto[] {
  const generated = targetScore == null ? null : generateParamsFromScore('FLASHCARD_QA', targetScore)
  const ops = allowedOps && allowedOps.length > 0
    ? allowedOps.map((o) => o.toUpperCase())
    : generated?.type === 'FLASHCARD_QA'
      ? [generated.params.operation]
    : ['ADD', 'SUBTRACT', 'MULTIPLY', 'DIVIDE']
  const difficulty = difficulties[0] ?? 'EASY'

  return Array.from({ length: count }, (_, i) => {
    const op = pickRandom(ops)
    const generatedForScore = targetScore == null ? null : generateParamsFromScore('FLASHCARD_QA', targetScore)
    const generatedParams = generatedForScore?.type === 'FLASHCARD_QA' ? generatedForScore.params : null
    const result = generatedParams != null
      ? buildFlashcardFromGeneratedParams(
          generatedParams.operation,
          generatedParams.firstOperand,
          generatedParams.secondOperand,
        )
      : generateMathFlashcard(op, difficulty)
    return {
      id: `math-gen-offline-${Date.now()}-${i}`,
      subjectId,
      subjectCode: 'default',
      type: 'FLASHCARD_QA',
      difficulty,
      prompt: result.prompt,
      expectedAnswers: [result.expectedAnswer],
      timeLimitSeconds: 60,
      mathOperation: generatedParams?.operation ?? op,
      mathComplexityScore: result.complexityScore,
    } as ExerciseDto
  })
}

export function buildSyntheticWordlePool(
  difficulties: string[],
  subjectId: string,
  targetScore?: number,
  count = 8,
): ExerciseDto[] {
  const difficulty = difficulties[0] ?? 'EASY'
  return Array.from({ length: count }, (_, i) => {
    const generated = targetScore == null ? null : generateParamsFromScore('WORDLE', targetScore)
    const params = generated?.type === 'WORDLE'
      ? generated.params
      : {
          answer: 'chat',
          wordLength: wordLengthForDifficulty(difficulty),
          maxAttempts: difficulty === 'HARD' || difficulty === 'VERY_HARD' ? 5 : 6,
          language: 'fr',
        }
    return {
      id: `wordle-gen-offline-${Date.now()}-${i}`,
      subjectId,
      subjectCode: 'WORD',
      type: 'WORDLE',
      difficulty,
      prompt: 'Trouvez le mot caché.',
      expectedAnswers: [],
      timeLimitSeconds: 120,
      wordleParams: params,
    } as ExerciseDto
  })
}

export function buildSyntheticAnagramPool(
  difficulties: string[],
  subjectId: string,
  targetScore?: number,
  count = 8,
): ExerciseDto[] {
  const difficulty = difficulties[0] ?? 'EASY'
  return Array.from({ length: count }, (_, i) => {
    const generated = targetScore == null ? null : generateParamsFromScore('ANAGRAM', targetScore)
    const params = generated?.type === 'ANAGRAM'
      ? generated.params
      : {
          answer: 'chat',
          scrambledLetters: scrambleAnagram('chat'),
          hintIntervalSeconds: 10,
          letterColorHint: true,
        }
    return {
      id: `anagram-gen-offline-${Date.now()}-${i}`,
      subjectId,
      subjectCode: 'WORD',
      type: 'ANAGRAM',
      difficulty,
      prompt: 'Recomposez le mot.',
      expectedAnswers: [],
      timeLimitSeconds: 120,
      anagramParams: params,
    } as ExerciseDto
  })
}
