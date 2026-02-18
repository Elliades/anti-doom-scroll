export interface NBackParamsDto {
  n: number
  sequence: string[]
  matchIndices: number[]
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
  nBackParams?: NBackParamsDto | null
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
