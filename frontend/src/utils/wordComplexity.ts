import type { AnagramParamsDto, WordleParamsDto } from '../types/api'

export interface RankedWord {
  word: string
  frequencyIndex: number
}

const RARE_LETTER_REGEX = /[kwz]/i

const WORDS_FR: RankedWord[] = [
  { word: 'chat', frequencyIndex: 35 },
  { word: 'jour', frequencyIndex: 48 },
  { word: 'main', frequencyIndex: 72 },
  { word: 'maison', frequencyIndex: 132 },
  { word: 'famille', frequencyIndex: 211 },
  { word: 'fromage', frequencyIndex: 332 },
  { word: 'cerise', frequencyIndex: 483 },
  { word: 'cascade', frequencyIndex: 731 },
  { word: 'volcan', frequencyIndex: 1140 },
  { word: 'zenith', frequencyIndex: 1550 },
  { word: 'whisky', frequencyIndex: 2130 },
  { word: 'zygote', frequencyIndex: 2680 },
]

const WORDS_EN: RankedWord[] = [
  { word: 'tree', frequencyIndex: 30 },
  { word: 'book', frequencyIndex: 66 },
  { word: 'house', frequencyIndex: 130 },
  { word: 'light', frequencyIndex: 250 },
  { word: 'planet', frequencyIndex: 420 },
  { word: 'forest', frequencyIndex: 680 },
  { word: 'rhythm', frequencyIndex: 1220 },
  { word: 'wizard', frequencyIndex: 1680 },
  { word: 'oxygen', frequencyIndex: 2060 },
  { word: 'puzzled', frequencyIndex: 2510 },
  { word: 'awkward', frequencyIndex: 2960 },
]

const ANAGRAM_MIN_RAW = 6 / 1000
const ANAGRAM_MAX_RAW = 5040 / 1
const WORDLE_MIN_RAW = (3 * 10) / 8
const WORDLE_MAX_RAW = (7 * 10) / 3 + 7

function normalize0to100(raw: number, minRaw: number, maxRaw: number): number {
  if (maxRaw <= minRaw) return 0
  const normalized = ((raw - minRaw) / (maxRaw - minRaw)) * 100
  return Math.max(0, Math.min(100, normalized))
}

function hasDoubleLetters(word: string): boolean {
  const seen = new Set<string>()
  for (const char of word) {
    if (seen.has(char)) return true
    seen.add(char)
  }
  return false
}

/**
 * Zipf-inspired lexical load for anagram solving.
 * Formula: Cb = factorial(wordLength) / frequencyIndex
 * where lower-rank (rarer) words increase lexical retrieval cost.
 */
export function anagramRawComplexity(wordLength: number, frequencyIndex: number): number {
  const boundedLength = Math.max(3, Math.min(7, Math.floor(wordLength)))
  const safeRank = Math.max(1, Math.floor(frequencyIndex))
  let factorial = 1
  for (let i = 2; i <= boundedLength; i++) factorial *= i
  return factorial / safeRank
}

/**
 * Wordle candidate complexity with fixed penalties.
 * Formula: Cb = (wordLength * 10) / maxAttempts + rareLetterPenalty + doubleLetterPenalty
 * Penalties: +4 for at least one rare letter (K/W/Z), +3 for duplicated letters.
 */
export function wordleRawComplexity(word: string, maxAttempts: number): number {
  const lengthTerm = (Math.max(3, Math.min(7, word.length)) * 10) / Math.max(3, Math.min(8, maxAttempts))
  const rarePenalty = RARE_LETTER_REGEX.test(word) ? 4 : 0
  const doublePenalty = hasDoubleLetters(word) ? 3 : 0
  return lengthTerm + rarePenalty + doublePenalty
}

/**
 * Normalized anagram complexity using theoretical French/English bounds.
 * Formula: C100 = ((Cb - Cmin) / (Cmax - Cmin)) * 100
 * with Cmin = 3!/1000 and Cmax = 7!/1.
 */
export function anagramComplexityScore(wordLength: number, frequencyIndex: number): number {
  return normalize0to100(anagramRawComplexity(wordLength, frequencyIndex), ANAGRAM_MIN_RAW, ANAGRAM_MAX_RAW)
}

/**
 * Normalized wordle complexity using theoretical bounds for 3..7 letters and 3..8 attempts.
 * Formula: C100 = ((Cb - Cmin) / (Cmax - Cmin)) * 100
 * with Cmin = (3*10)/8 and Cmax = (7*10)/3 + 7.
 */
export function wordleComplexityScore(word: string, maxAttempts: number): number {
  return normalize0to100(wordleRawComplexity(word, maxAttempts), WORDLE_MIN_RAW, WORDLE_MAX_RAW)
}

export function lexiconForLanguage(language: string): RankedWord[] {
  return language === 'en' ? WORDS_EN : WORDS_FR
}

export function estimateWordleComplexityFromParams(params: WordleParamsDto): number {
  return wordleComplexityScore(params.answer.toLowerCase(), params.maxAttempts ?? 6)
}

export function estimateAnagramComplexityFromParams(params: AnagramParamsDto): number {
  const language = 'fr'
  const entry = lexiconForLanguage(language).find((w) => w.word === params.answer.toLowerCase())
  const rank = entry?.frequencyIndex ?? 1500
  return anagramComplexityScore(params.answer.length, rank)
}
