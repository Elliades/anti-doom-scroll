export interface NBackParamsDto {
  n: number
  sequence: string[]
  matchIndices: number[]
}

export interface NBackGridParamsDto {
  n: number
  sequence: number[]
  matchIndices: number[]
  gridSize?: number
}

export interface GridStimulusDto {
  position: number
  color: string
}

export interface DualNBackGridParamsDto {
  n: number
  sequence: GridStimulusDto[]
  matchPositionIndices: number[]
  matchColorIndices: number[]
  colors: string[]
  gridSize?: number
}

export interface DualNBackCardParamsDto {
  n: number
  sequence: string[]
  matchColorIndices: number[]
  matchNumberIndices: number[]
}

export interface MemoryCardParamsDto {
  pairCount: number
  symbols: string[]
  /** Pre-shuffled deck from backend (stable per session); if absent, frontend shuffles locally. */
  shuffledDeck?: string[] | null
}

export interface SumPairParamsDto {
  staticNumbers: number[]
  pairsPerRound: number
  minValue?: number
  maxValue?: number
}

export interface SumPairRoundDto {
  static: number
  cards: number[]
}

export interface SumPairGroupDto {
  static: number
  color: string
  cards: number[]
}

export interface SumPairCardDto {
  value: number
  static: number
  color: string
}

export interface ImagePairParamsDto {
  pairCount: number
  maxPairsPerBackground: number
  colorCount: number
}

export interface ImagePairCardDto {
  backgroundId: number
  imageId: string
  backgroundColorHex: string | null
}

export interface AnagramParamsDto {
  scrambledLetters: string[]
  answer: string
  /** Hint every N seconds of inactivity; default 10. */
  hintIntervalSeconds?: number
  /** When true, filled slots show green (correct) or red (wrong) — another kind of hint. */
  letterColorHint?: boolean
}

export interface WordleParamsDto {
  answer: string
  wordLength: number
  maxAttempts?: number
  language?: string
}

export interface ExerciseDto {
  id: string
  subjectId: string
  subjectCode: string | null
  type: string
  difficulty: string
  prompt: string
  expectedAnswers: string[]
  timeLimitSeconds: number
  /** FLASHCARD_QA math: ADD, SUBTRACT, MULTIPLY, DIVIDE — for display label (Sum, Subtraction, etc.) */
  mathOperation?: string | null
  /** API may return nBackParams or nbackParams (casing varies) */
  nBackParams?: NBackParamsDto | null
  nbackParams?: NBackParamsDto | null
  /** API may return nBackGridParams or nbackGridParams (casing varies) */
  nBackGridParams?: NBackGridParamsDto | null
  nbackGridParams?: NBackGridParamsDto | null
  dualNBackGridParams?: DualNBackGridParamsDto | null
  dualNbackGridParams?: DualNBackGridParamsDto | null
  dualNBackCardParams?: DualNBackCardParamsDto | null
  dualNbackCardParams?: DualNBackCardParamsDto | null
  memoryCardParams?: MemoryCardParamsDto | null
  sumPairParams?: SumPairParamsDto | null
  sumPairRounds?: SumPairRoundDto[] | null
  sumPairGroups?: SumPairGroupDto[] | null
  sumPairDeck?: SumPairCardDto[] | null
  imagePairParams?: ImagePairParamsDto | null
  imagePairDeck?: ImagePairCardDto[] | null
  anagramParams?: AnagramParamsDto | null
  wordleParams?: WordleParamsDto | null
}

export interface SessionStepDto {
  stepIndex: number
  difficulty: string
  exercise: ExerciseDto
}

export interface SessionResponseDto {
  profileId: string
  steps: SessionStepDto[]
  sessionDefaultSeconds: number
  lowBatteryModeSeconds: number
}

// Journey
export interface JourneyStepDefDto {
  stepIndex: number
  type: string
  config: Record<string, unknown>
}

export interface JourneyDto {
  code: string
  name: string | null
  steps: JourneyStepDefDto[]
}

export interface ReflectionContentDto {
  title: string
  body: string
}

export interface ChapterSeriesContentDto {
  chapters: string[]
  currentChapterIndex: number
  session: SessionResponseDto | null
}

export interface JourneyStepContentDto {
  stepIndex: number
  type: string
  session?: SessionResponseDto | null
  reflection?: ReflectionContentDto | null
  chapterSeries?: ChapterSeriesContentDto | null
}

// Ladder mode
export interface LadderStateDto {
  ladderCode: string
  currentLevelIndex: number
  recentScores: number[]
  overallScoreSum: number
  overallTotal: number
}

export interface LadderSessionResponseDto {
  profileId: string
  mode: string
  exercise: ExerciseDto
  ladderState: LadderStateDto
  sessionDefaultSeconds: number
  lowBatteryModeSeconds: number
}

export interface LadderNextResponseDto {
  exercise: ExerciseDto | null
  ladderState: LadderStateDto
  levelChanged?: { from: number; to: number; direction: string } | null
}
