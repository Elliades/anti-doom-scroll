import { describe, it, expect, vi } from 'vitest'
import {
  generateMathFlashcard,
  generateNBackSequence,
  inferSuitCount,
  generateNBackGridSequence,
  generateDualNBackCardSequence,
  inferDualCardSuitCount,
  generateDualNBackGridSequence,
  shuffleMemoryDeck,
  scrambleAnagram,
  generateSumPairGroups,
  hydrateExercise,
  buildSyntheticMemoryCardPool,
  buildSyntheticSumPairPool,
  buildSyntheticMathFlashcardPool,
  buildSyntheticWordlePool,
  buildSyntheticAnagramPool,
  applyScoreDrivenParams,
} from './offlineGenerators'
import type { ExerciseDto } from '../types/api'

// ---------------------------------------------------------------------------
// Math Flashcard Generator
// ---------------------------------------------------------------------------

describe('generateMathFlashcard', () => {
  it('generates valid ADD problems', () => {
    for (let i = 0; i < 20; i++) {
      const r = generateMathFlashcard('ADD', 'EASY')
      expect(r.prompt).toMatch(/^What is \d+ \+ \d+\?$/)
      const [a, b] = r.prompt.replace('What is ', '').replace('?', '').split(' + ').map(Number)
      expect(r.expectedAnswer).toBe(String(a + b))
      expect(r.complexityScore).toBeGreaterThanOrEqual(0)
    }
  })

  it('generates valid SUBTRACT problems (result >= 0)', () => {
    for (let i = 0; i < 20; i++) {
      const r = generateMathFlashcard('SUBTRACT', 'MEDIUM')
      expect(r.prompt).toMatch(/^What is \d+ \u2212 \d+\?$/)
      expect(Number(r.expectedAnswer)).toBeGreaterThanOrEqual(0)
    }
  })

  it('generates valid MULTIPLY problems', () => {
    for (let i = 0; i < 20; i++) {
      const r = generateMathFlashcard('MULTIPLY', 'EASY')
      expect(r.prompt).toMatch(/^What is \d+ \u00d7 \d+\?$/)
      const [a, b] = r.prompt.replace('What is ', '').replace('?', '').split(' \u00d7 ').map(Number)
      expect(r.expectedAnswer).toBe(String(a * b))
    }
  })

  it('generates valid DIVIDE problems (clean division)', () => {
    for (let i = 0; i < 20; i++) {
      const r = generateMathFlashcard('DIVIDE', 'EASY')
      expect(r.prompt).toMatch(/^What is \d+ \u00f7 \d+\?$/)
      const parts = r.prompt.replace('What is ', '').replace('?', '').split(' \u00f7 ').map(Number)
      expect(parts[0] % parts[1]).toBe(0)
      expect(r.expectedAnswer).toBe(String(parts[0] / parts[1]))
    }
  })

  it('produces different problems on successive calls', () => {
    const prompts = new Set<string>()
    for (let i = 0; i < 30; i++) {
      prompts.add(generateMathFlashcard('ADD', 'MEDIUM').prompt)
    }
    expect(prompts.size).toBeGreaterThan(1)
  })
})

// ---------------------------------------------------------------------------
// N-Back Sequence Generator
// ---------------------------------------------------------------------------

describe('generateNBackSequence', () => {
  it('returns correct sequence length', () => {
    const { sequence, matchIndices } = generateNBackSequence(1, 2, 15)
    expect(sequence).toHaveLength(15)
    for (const idx of matchIndices) {
      expect(idx).toBeGreaterThanOrEqual(1)
      expect(idx).toBeLessThan(15)
    }
  })

  it('matchIndices correctly identify n-back matches', () => {
    for (let trial = 0; trial < 10; trial++) {
      const n = 2
      const { sequence, matchIndices } = generateNBackSequence(n, 4, 20)
      const matchSet = new Set(matchIndices)
      for (let i = n; i < sequence.length; i++) {
        if (matchSet.has(i)) {
          expect(sequence[i]).toBe(sequence[i - n])
        }
      }
    }
  })

  it('generates cards from expected suits', () => {
    const { sequence } = generateNBackSequence(1, 2, 12)
    for (const card of sequence) {
      const suit = card.slice(-1)
      expect('CD').toContain(suit)
    }
  })

  it('produces different sequences on successive calls', () => {
    const seqs = new Set<string>()
    for (let i = 0; i < 10; i++) {
      seqs.add(JSON.stringify(generateNBackSequence(1, 4, 12).sequence))
    }
    expect(seqs.size).toBeGreaterThan(1)
  })
})

describe('inferSuitCount', () => {
  it('infers 1 suit from single-suit sequence', () => {
    expect(inferSuitCount(['AC', '2C', 'KC'])).toBe(1)
  })

  it('infers 4 suits from diverse sequence', () => {
    expect(inferSuitCount(['AC', '2D', '3H', '4S'])).toBe(4)
  })
})

// ---------------------------------------------------------------------------
// N-Back Grid Sequence Generator
// ---------------------------------------------------------------------------

describe('generateNBackGridSequence', () => {
  it('returns sequence of valid cell indices', () => {
    const gridSize = 3
    const { sequence, matchIndices } = generateNBackGridSequence(1, gridSize, 12)
    expect(sequence).toHaveLength(12)
    for (const cell of sequence) {
      expect(cell).toBeGreaterThanOrEqual(0)
      expect(cell).toBeLessThan(gridSize * gridSize)
    }
    for (const idx of matchIndices) {
      expect(sequence[idx]).toBe(sequence[idx - 1])
    }
  })
})

// ---------------------------------------------------------------------------
// Dual N-Back Card Sequence Generator
// ---------------------------------------------------------------------------

describe('generateDualNBackCardSequence', () => {
  it('returns correct structure with independent match channels', () => {
    const n = 1
    const { sequence, matchColorIndices, matchNumberIndices } = generateDualNBackCardSequence(n, 4, 15)
    expect(sequence).toHaveLength(15)

    for (const idx of matchColorIndices) {
      const prevSuit = sequence[idx - n].slice(-1)
      const curSuit = sequence[idx].slice(-1)
      expect(curSuit).toBe(prevSuit)
    }

    for (const idx of matchNumberIndices) {
      const prevRank = sequence[idx - n].slice(0, -1)
      const curRank = sequence[idx].slice(0, -1)
      expect(curRank).toBe(prevRank)
    }
  })
})

describe('inferDualCardSuitCount', () => {
  it('infers correct suit count', () => {
    expect(inferDualCardSuitCount(['AC', '2D', '3C'])).toBe(2)
  })
})

// ---------------------------------------------------------------------------
// Dual N-Back Grid Sequence Generator
// ---------------------------------------------------------------------------

describe('generateDualNBackGridSequence', () => {
  it('returns correct structure with position and color channels', () => {
    const n = 1
    const gridSize = 3
    const colorCount = 4
    const result = generateDualNBackGridSequence(n, gridSize, colorCount, 15)
    expect(result.sequence).toHaveLength(15)
    expect(result.colors).toHaveLength(colorCount)

    for (const stimulus of result.sequence) {
      expect(stimulus.position).toBeGreaterThanOrEqual(0)
      expect(stimulus.position).toBeLessThan(gridSize * gridSize)
      expect(result.colors).toContain(stimulus.color)
    }

    for (const idx of result.matchPositionIndices) {
      expect(result.sequence[idx].position).toBe(result.sequence[idx - n].position)
    }

    for (const idx of result.matchColorIndices) {
      expect(result.sequence[idx].color).toBe(result.sequence[idx - n].color)
    }
  })
})

// ---------------------------------------------------------------------------
// Memory Card Deck Shuffler
// ---------------------------------------------------------------------------

describe('shuffleMemoryDeck', () => {
  it('produces a deck with 2x the symbols', () => {
    const symbols = ['A', 'B', 'C']
    const deck = shuffleMemoryDeck(symbols)
    expect(deck).toHaveLength(6)
    expect(deck.filter((c) => c === 'A')).toHaveLength(2)
    expect(deck.filter((c) => c === 'B')).toHaveLength(2)
    expect(deck.filter((c) => c === 'C')).toHaveLength(2)
  })

  it('shuffles into different orders', () => {
    const symbols = ['A', 'B', 'C', 'D', 'E']
    const orders = new Set<string>()
    for (let i = 0; i < 20; i++) {
      orders.add(JSON.stringify(shuffleMemoryDeck(symbols)))
    }
    expect(orders.size).toBeGreaterThan(1)
  })
})

// ---------------------------------------------------------------------------
// Anagram Scrambler
// ---------------------------------------------------------------------------

describe('scrambleAnagram', () => {
  it('uses all the same letters as the answer', () => {
    const answer = 'hello'
    const scrambled = scrambleAnagram(answer)
    expect(scrambled).toHaveLength(answer.length)
    expect([...scrambled].sort()).toEqual([...answer].sort())
  })

  it('usually produces a different order', () => {
    const answer = 'abcdef'
    let differentCount = 0
    for (let i = 0; i < 20; i++) {
      const s = scrambleAnagram(answer)
      if (s.join('') !== answer) differentCount++
    }
    expect(differentCount).toBeGreaterThan(0)
  })
})

// ---------------------------------------------------------------------------
// Sum Pair Generator
// ---------------------------------------------------------------------------

describe('generateSumPairGroups', () => {
  it('generates valid pairs for single static', () => {
    const result = generateSumPairGroups([5], 4, 1, 50)
    expect(result.groups).toHaveLength(1)
    expect(result.groups[0].cards).toHaveLength(8) // 4 pairs = 8 cards
    const cards = result.groups[0].cards
    const cardSet = new Set(cards)
    expect(cardSet.size).toBe(8) // all distinct

    // each value should have a partner that differs by static
    for (const v of cards) {
      const hasPartner = cards.includes(v + 5) || cards.includes(v - 5)
      expect(hasPartner).toBe(true)
    }
  })

  it('generates valid deck with all group cards', () => {
    const result = generateSumPairGroups([3, 7], 3, 1, 99)
    const totalCards = result.groups.reduce((sum, g) => sum + g.cards.length, 0)
    expect(result.deck).toHaveLength(totalCards)
  })

  it('no chain violation: no x, x+K, x+2K in same group', () => {
    for (let trial = 0; trial < 10; trial++) {
      const result = generateSumPairGroups([5], 4, 1, 50)
      const cards = new Set(result.groups[0].cards)
      for (const x of cards) {
        if (cards.has(x + 5)) {
          expect(cards.has(x - 5)).toBe(false)
        }
      }
    }
  })
})

// ---------------------------------------------------------------------------
// hydrateExercise
// ---------------------------------------------------------------------------

describe('hydrateExercise', () => {
  const baseExercise: ExerciseDto = {
    id: 'test-1',
    subjectId: 'sub-1',
    subjectCode: 'default',
    type: 'FLASHCARD_QA',
    difficulty: 'EASY',
    prompt: 'What is 2 + 3?',
    expectedAnswers: ['5'],
    timeLimitSeconds: 60,
    mathOperation: 'ADD',
  }

  it('regenerates math flashcard with different problem', async () => {
    const results = new Set<string>()
    for (let i = 0; i < 20; i++) {
      const hydrated = await hydrateExercise(baseExercise)
      results.add(hydrated.prompt)
      expect(hydrated.type).toBe('FLASHCARD_QA')
      expect(hydrated.mathOperation).toBe('ADD')
    }
    expect(results.size).toBeGreaterThan(1)
  })

  it('regenerates memory card deck order', async () => {
    const memEx: ExerciseDto = {
      id: 'mem-1',
      subjectId: 'sub-1',
      subjectCode: 'MEMORY',
      type: 'MEMORY_CARD_PAIRS',
      difficulty: 'EASY',
      prompt: 'Find all matching pairs.',
      expectedAnswers: [],
      timeLimitSeconds: 120,
      memoryCardParams: {
        pairCount: 3,
        symbols: ['A', 'B', 'C'],
        shuffledDeck: ['A', 'B', 'C', 'A', 'B', 'C'],
      },
    }
    const orders = new Set<string>()
    for (let i = 0; i < 20; i++) {
      const hydrated = await hydrateExercise(memEx)
      orders.add(JSON.stringify(hydrated.memoryCardParams?.shuffledDeck))
      expect(hydrated.memoryCardParams?.shuffledDeck).toHaveLength(6)
    }
    expect(orders.size).toBeGreaterThan(1)
  })

  it('regenerates n-back sequence', async () => {
    const nbackEx: ExerciseDto = {
      id: 'nb-1',
      subjectId: 'sub-1',
      subjectCode: 'nback',
      type: 'N_BACK',
      difficulty: 'EASY',
      prompt: 'N-Back',
      expectedAnswers: [],
      timeLimitSeconds: 60,
      nBackParams: {
        n: 1,
        sequence: ['AC', '2C', 'AC', '3C', 'KC', '5C', '5C', '7C', '8C', '9C', '10C', 'JC'],
        matchIndices: [2, 6],
      },
    }
    const seqs = new Set<string>()
    for (let i = 0; i < 10; i++) {
      const hydrated = await hydrateExercise(nbackEx)
      const p = hydrated.nBackParams ?? hydrated.nbackParams
      expect(p?.sequence).toHaveLength(12)
      seqs.add(JSON.stringify(p?.sequence))
      // verify matchIndices consistency
      const matchSet = new Set(p!.matchIndices)
      for (let j = p!.n; j < p!.sequence.length; j++) {
        if (matchSet.has(j)) {
          expect(p!.sequence[j]).toBe(p!.sequence[j - p!.n])
        }
      }
    }
    expect(seqs.size).toBeGreaterThan(1)
  })

  it('regenerates anagram scramble', async () => {
    const anaEx: ExerciseDto = {
      id: 'ana-1',
      subjectId: 'sub-1',
      subjectCode: 'WORD',
      type: 'ANAGRAM',
      difficulty: 'EASY',
      prompt: 'Unscramble',
      expectedAnswers: [],
      timeLimitSeconds: 60,
      anagramParams: {
        scrambledLetters: ['e', 'h', 'l', 'l', 'o'],
        answer: 'hello',
      },
    }
    const hydrated = await hydrateExercise(anaEx)
    expect(hydrated.anagramParams?.scrambledLetters.sort()).toEqual([...'hello'].sort())
  })

  it('regenerates wordle answer', async () => {
    vi.mock('../utils/wordleWordLoader', () => ({
      loadWordListWords: vi.fn().mockResolvedValue(['apple', 'grape', 'lemon', 'mango', 'peach', 'melon', 'berry']),
    }))

    const wordleEx: ExerciseDto = {
      id: 'wrd-1',
      subjectId: 'sub-1',
      subjectCode: 'WORD',
      type: 'WORDLE',
      difficulty: 'MEDIUM',
      prompt: 'Guess the word',
      expectedAnswers: [],
      timeLimitSeconds: 60,
      wordleParams: {
        answer: 'apple',
        wordLength: 5,
        maxAttempts: 6,
        language: 'en',
      },
    }
    const hydrated = await hydrateExercise(wordleEx)
    expect(hydrated.wordleParams?.answer).toBeTruthy()
    expect(hydrated.wordleParams?.wordLength).toBe(5)
  })

  it('returns unchanged exercise for unknown types', async () => {
    const ex: ExerciseDto = {
      id: 'unk-1',
      subjectId: 'sub-1',
      subjectCode: null,
      type: 'SOME_UNKNOWN_TYPE',
      difficulty: 'EASY',
      prompt: 'test',
      expectedAnswers: [],
      timeLimitSeconds: 60,
    }
    const hydrated = await hydrateExercise(ex)
    expect(hydrated).toEqual(ex)
  })

  it('regenerates sum pair groups and deck', async () => {
    const spEx: ExerciseDto = {
      id: 'sp-1',
      subjectId: 'sub-1',
      subjectCode: 'MEMORY',
      type: 'SUM_PAIR',
      difficulty: 'EASY',
      prompt: 'Find pairs',
      expectedAnswers: [],
      timeLimitSeconds: 120,
      sumPairParams: {
        staticNumbers: [5],
        pairsPerRound: 3,
        minValue: 1,
        maxValue: 50,
      },
      sumPairGroups: [{ static: 5, color: '#3b82f6', cards: [10, 15, 20, 25, 30, 35] }],
      sumPairDeck: [
        { value: 10, static: 5, color: '#3b82f6' },
        { value: 15, static: 5, color: '#3b82f6' },
      ],
    }
    const decks = new Set<string>()
    for (let i = 0; i < 10; i++) {
      const hydrated = await hydrateExercise(spEx)
      expect(hydrated.sumPairGroups).toHaveLength(1)
      expect(hydrated.sumPairGroups![0].cards).toHaveLength(6)
      decks.add(JSON.stringify(hydrated.sumPairDeck))
    }
    expect(decks.size).toBeGreaterThan(1)
  })
})

// ---------------------------------------------------------------------------
// Synthetic Pool Builders
// ---------------------------------------------------------------------------

describe('buildSyntheticMemoryCardPool', () => {
  it('creates 8 exercises with valid memory params', () => {
    const pool = buildSyntheticMemoryCardPool(['EASY'], 'sub-1')
    expect(pool).toHaveLength(8)
    for (const ex of pool) {
      expect(ex.type).toBe('MEMORY_CARD_PAIRS')
      expect(ex.memoryCardParams).toBeDefined()
      expect(ex.memoryCardParams!.symbols.length).toBeGreaterThanOrEqual(3)
      expect(ex.memoryCardParams!.shuffledDeck).toBeDefined()
    }
  })

  it('adjusts pair count by difficulty', () => {
    const easy = buildSyntheticMemoryCardPool(['ULTRA_EASY'], 'sub-1')
    const hard = buildSyntheticMemoryCardPool(['HARD'], 'sub-1')
    expect(easy[0].memoryCardParams!.pairCount).toBe(3)
    expect(hard[0].memoryCardParams!.pairCount).toBe(6)
  })
})

describe('buildSyntheticSumPairPool', () => {
  it('creates exercises with valid sum pair params', () => {
    const pool = buildSyntheticSumPairPool(['EASY'], 'sub-1')
    expect(pool.length).toBeGreaterThan(0)
    expect(pool.length).toBeLessThanOrEqual(8)
    for (const ex of pool) {
      expect(ex.type).toBe('SUM_PAIR')
      expect(ex.sumPairParams).toBeDefined()
      expect(ex.sumPairGroups).toBeDefined()
      expect(ex.sumPairDeck).toBeDefined()
    }
  })
})

describe('buildSyntheticMathFlashcardPool', () => {
  it('creates 8 exercises with valid math params', () => {
    const pool = buildSyntheticMathFlashcardPool(['MEDIUM'], 'sub-1')
    expect(pool).toHaveLength(8)
    for (const ex of pool) {
      expect(ex.type).toBe('FLASHCARD_QA')
      expect(ex.mathOperation).toBeTruthy()
      expect(ex.prompt).toMatch(/^What is /)
      expect(ex.expectedAnswers).toHaveLength(1)
    }
  })

  it('respects allowed operations filter', () => {
    const pool = buildSyntheticMathFlashcardPool(['EASY'], 'sub-1', ['ADD'])
    for (const ex of pool) {
      expect(ex.mathOperation).toBe('ADD')
    }
  })
})

describe('buildSyntheticWordlePool', () => {
  it('creates wordle exercises with params', () => {
    const pool = buildSyntheticWordlePool(['MEDIUM'], 'sub-1', 0.4)
    expect(pool).toHaveLength(8)
    for (const ex of pool) {
      expect(ex.type).toBe('WORDLE')
      expect(ex.wordleParams?.answer).toBeTruthy()
      expect(ex.wordleParams?.wordLength).toBeGreaterThanOrEqual(3)
      expect(ex.wordleParams?.wordLength).toBeLessThanOrEqual(7)
    }
  })
})

describe('buildSyntheticAnagramPool', () => {
  it('creates anagram exercises with params', () => {
    const pool = buildSyntheticAnagramPool(['MEDIUM'], 'sub-1', 0.4)
    expect(pool).toHaveLength(8)
    for (const ex of pool) {
      expect(ex.type).toBe('ANAGRAM')
      expect(ex.anagramParams?.answer).toBeTruthy()
      expect(ex.anagramParams?.scrambledLetters).toHaveLength(ex.anagramParams?.answer.length ?? 0)
    }
  })
})

describe('applyScoreDrivenParams', () => {
  it('branches mappings for all mapped exercise types', () => {
    const base: ExerciseDto = {
      id: 'seed',
      subjectId: 's1',
      subjectCode: 'default',
      type: 'FLASHCARD_QA',
      difficulty: 'EASY',
      prompt: 'x',
      expectedAnswers: [],
      timeLimitSeconds: 60,
    }

    expect(applyScoreDrivenParams({ ...base, type: 'FLASHCARD_QA' }, 0.6).mathOperation).toBeTruthy()
    expect(applyScoreDrivenParams({ ...base, type: 'SUM_PAIR' }, 0.6).sumPairParams).toBeDefined()
    expect(applyScoreDrivenParams({ ...base, type: 'MEMORY_CARD_PAIRS' }, 0.6).memoryCardParams?.pairCount).toBeGreaterThan(0)
    expect(applyScoreDrivenParams({ ...base, type: 'WORDLE' }, 0.6).wordleParams?.wordLength).toBeGreaterThanOrEqual(3)
    expect(applyScoreDrivenParams({ ...base, type: 'ANAGRAM' }, 0.6).anagramParams?.answer.length).toBeGreaterThanOrEqual(3)
    expect(applyScoreDrivenParams({ ...base, type: 'DIGIT_SPAN' }, 0.6).digitSpanParams?.startLength).toBeGreaterThanOrEqual(3)
    expect(applyScoreDrivenParams({ ...base, type: 'ESTIMATION' }, 0.6).estimationParams?.toleranceFactor).toBeGreaterThan(0)
    expect((applyScoreDrivenParams({ ...base, type: 'N_BACK' }, 0.6).nBackParams ?? applyScoreDrivenParams({ ...base, type: 'N_BACK' }, 0.6).nbackParams)?.n).toBeGreaterThanOrEqual(1)
    expect((applyScoreDrivenParams({ ...base, type: 'N_BACK_GRID' }, 0.6).nBackGridParams ?? applyScoreDrivenParams({ ...base, type: 'N_BACK_GRID' }, 0.6).nbackGridParams)?.n).toBeGreaterThanOrEqual(1)
    expect((applyScoreDrivenParams({ ...base, type: 'DUAL_NBACK_CARD' }, 0.6).dualNBackCardParams ?? applyScoreDrivenParams({ ...base, type: 'DUAL_NBACK_CARD' }, 0.6).dualNbackCardParams)?.n).toBeGreaterThanOrEqual(1)
    expect((applyScoreDrivenParams({ ...base, type: 'DUAL_NBACK_GRID' }, 0.6).dualNBackGridParams ?? applyScoreDrivenParams({ ...base, type: 'DUAL_NBACK_GRID' }, 0.6).dualNbackGridParams)?.n).toBeGreaterThanOrEqual(1)
    expect(applyScoreDrivenParams({ ...base, type: 'MATH_CHAIN' }, 0.6).mathChainParams?.steps.length).toBeGreaterThan(0)
    expect(applyScoreDrivenParams({ ...base, type: 'IMAGE_PAIR' }, 0.6).imagePairParams).toBeDefined()
  })
})
