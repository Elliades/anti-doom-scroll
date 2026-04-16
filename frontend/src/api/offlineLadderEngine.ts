import type {
  ExerciseDto,
  LadderMixNextResponseDto,
  LadderMixSessionResponseDto,
  LadderMixStateDto,
  LadderNextResponseDto,
  LadderSessionResponseDto,
  LadderStateDto,
  PerLadderStateDto,
} from '../types/api'
import { bundledDataUrl } from '../utils/bundledDataUrl'
import { loadCatalogOfflineBundle } from './offlineCatalog'
import {
  hydrateExercise,
  buildSyntheticMemoryCardPool,
  buildSyntheticSumPairPool,
  buildSyntheticMathFlashcardPool,
} from './offlineGenerators'

const OFFLINE_PROFILE_ID = 'offline-local'
const SESSION_DEFAULT_SECONDS = 180
const LOW_BATTERY_MODE_SECONDS = 45

export interface OfflineLadderLevelDef {
  levelIndex: number
  allowedDifficulties: string[]
  subjectCodes: string[]
  exerciseIds?: string[] | null
  exerciseParamFilter?: Record<string, string[]> | null
  allowedTypes?: string[] | null
}

export interface OfflineLadderConfigDef {
  name?: string | null
  thresholds: {
    minScoreToStay: number
    minScoreToAdvance: number
    answersNeededToAdvance: number
  }
  levels: OfflineLadderLevelDef[]
}

export interface LadderOfflineDataBundle {
  ladders: { code: string; name: string | null; levelCount: number }[]
  mixes: { code: string; name: string | null; ladderCodes: string[] }[]
  configs: Record<string, OfflineLadderConfigDef>
}

let ladderDataCache: LadderOfflineDataBundle | null = null

export async function loadLadderOfflineBundle(): Promise<LadderOfflineDataBundle> {
  if (ladderDataCache) return ladderDataCache
  const url = bundledDataUrl('ladder-offline.json')
  const res = await fetch(url)
  if (!res.ok) {
    throw new Error(
      `Missing bundled ladder data (${res.status}). Run \`npm run export:ladder-offline\` with the backend up, then rebuild.`
    )
  }
  ladderDataCache = (await res.json()) as LadderOfflineDataBundle
  return ladderDataCache
}

function getConfig(bundle: LadderOfflineDataBundle, ladderCode: string): OfflineLadderConfigDef | null {
  return bundle.configs[ladderCode] ?? null
}

/** Mirrors [LadderConfig.getAnswersNeededToAdvance]. */
export function getAnswersNeededToAdvance(
  ladderCode: string,
  levelIndex: number,
  config: OfflineLadderConfigDef
): number {
  if (ladderCode === 'nback' || ladderCode === 'pair') return 1
  if (levelIndex < 5) return 1
  return config.thresholds.answersNeededToAdvance
}

function exerciseParamStringMap(ex: ExerciseDto): Record<string, string> {
  const m: Record<string, string> = {}
  if (ex.mathOperation) m.operation = ex.mathOperation
  const np = ex.nBackParams ?? ex.nbackParams
  if (np?.n != null) m.n = String(np.n)
  const ng = ex.nBackGridParams ?? ex.nbackGridParams
  if (ng?.n != null) m.n = String(ng.n)
  if (ng?.gridSize != null) m.gridSize = String(ng.gridSize)
  const dg = ex.dualNBackGridParams ?? ex.dualNbackGridParams
  if (dg?.n != null) m.n = String(dg.n)
  if (dg?.gridSize != null) m.gridSize = String(dg.gridSize)
  const dc = ex.dualNBackCardParams ?? ex.dualNbackCardParams
  if (dc?.n != null) m.n = String(dc.n)
  if (ex.wordleParams?.language) m.language = ex.wordleParams.language
  if (ex.digitSpanParams) {
    m.startLength = String(ex.digitSpanParams.startLength)
    m.maxLength = String(ex.digitSpanParams.maxLength)
  }

  const suit = inferSuitCount(ex)
  if (suit != null) m.suitCount = suit

  return m
}

/** Infer suit count for N_BACK / DUAL_NBACK_CARD from sequence (card suit = last char). */
function inferSuitCount(ex: ExerciseDto): string | undefined {
  const np = ex.nBackParams ?? ex.nbackParams
  if (np?.sequence?.length) {
    const suits = new Set(np.sequence.map((s) => s.slice(-1)))
    if (suits.size >= 1 && suits.size <= 4) return String(suits.size)
  }
  const dc = ex.dualNBackCardParams ?? ex.dualNbackCardParams
  if (dc?.sequence?.length) {
    const suits = new Set(dc.sequence.map((s) => s.slice(-1)))
    if (suits.size >= 1 && suits.size <= 4) return String(suits.size)
  }
  return undefined
}

function matchesParamFilter(ex: ExerciseDto, filter: Record<string, string[]>): boolean {
  const params = exerciseParamStringMap(ex)
  return Object.entries(filter).every(([key, allowedValues]) => {
    const actual = params[key]
    return actual != null && allowedValues.some((v) => v === actual)
  })
}

function pickRandom<T>(arr: T[]): T | null {
  if (arr.length === 0) return null
  return arr[Math.floor(Math.random() * arr.length)]!
}

/**
 * Mirrors backend [LadderExercisePicker.pick].
 * Builds candidate pool from catalog + synthetic generated exercises,
 * then applies filters and picks randomly.
 */
export function pickExerciseForLevel(
  level: OfflineLadderLevelDef,
  allExercises: ExerciseDto[]
): ExerciseDto | null {
  if (level.exerciseIds?.length) {
    const idSet = new Set(level.exerciseIds)
    const pool = allExercises.filter((e) => idSet.has(e.id))
    return pickRandom(pool)
  }

  const catalogPool = allExercises.filter((e) => {
    if (level.subjectCodes.length > 0) {
      const sc = e.subjectCode ?? ''
      if (!level.subjectCodes.includes(sc)) return false
    }
    if (!level.allowedDifficulties.includes(e.difficulty)) return false
    return true
  })

  const syntheticPool = buildSyntheticPools(level, allExercises)

  let pool = [...catalogPool, ...syntheticPool]

  if (level.allowedTypes?.length) {
    pool = pool.filter((e) => level.allowedTypes!.includes(e.type))
  }
  if (level.exerciseParamFilter && Object.keys(level.exerciseParamFilter).length > 0) {
    pool = pool.filter((e) => matchesParamFilter(e, level.exerciseParamFilter!))
  }

  return pickRandom(pool)
}

function allowsType(level: OfflineLadderLevelDef, type: string): boolean {
  return !level.allowedTypes?.length || level.allowedTypes.includes(type)
}

function findSubjectId(allExercises: ExerciseDto[], subjectCode: string): string | null {
  const ex = allExercises.find((e) => e.subjectCode === subjectCode)
  return ex?.subjectId ?? null
}

function buildSyntheticPools(level: OfflineLadderLevelDef, allExercises: ExerciseDto[]): ExerciseDto[] {
  const synthetic: ExerciseDto[] = []

  if (level.subjectCodes.includes('MEMORY') && allowsType(level, 'MEMORY_CARD_PAIRS')) {
    const sid = findSubjectId(allExercises, 'MEMORY')
    if (sid) synthetic.push(...buildSyntheticMemoryCardPool(level.allowedDifficulties, sid))
  }

  if (level.subjectCodes.includes('MEMORY') && allowsType(level, 'SUM_PAIR')) {
    const sid = findSubjectId(allExercises, 'MEMORY')
    if (sid) synthetic.push(...buildSyntheticSumPairPool(level.allowedDifficulties, sid))
  }

  if (level.subjectCodes.includes('default') && allowsType(level, 'FLASHCARD_QA')) {
    const sid = findSubjectId(allExercises, 'default')
    const filterOps = level.exerciseParamFilter?.operation
    if (sid) synthetic.push(...buildSyntheticMathFlashcardPool(level.allowedDifficulties, sid, filterOps))
  }

  return synthetic
}

export async function offlineStartLadderSession(ladderCode: string): Promise<LadderSessionResponseDto> {
  const bundle = await loadLadderOfflineBundle()
  const { allExercises } = await loadCatalogOfflineBundle()
  const cfg =
    getConfig(bundle, ladderCode) ?? getConfig(bundle, 'default') ?? Object.values(bundle.configs)[0]
  if (!cfg) throw new Error('No ladder config in ladder-offline.json')

  const resolvedCode = bundle.configs[ladderCode] ? ladderCode : 'default'
  const level0 = cfg.levels.find((l) => l.levelIndex === 0)
  if (!level0) throw new Error(`Ladder "${resolvedCode}" has no level 0`)

  const picked = pickExerciseForLevel(level0, allExercises)
  if (!picked) throw new Error(`No exercise available for ladder "${resolvedCode}" level 0 (regenerate catalog-offline.json).`)
  const exercise = await hydrateExercise(picked)

  const ladderState: LadderStateDto = {
    ladderCode: resolvedCode,
    currentLevelIndex: 0,
    recentScores: [],
    overallScoreSum: 0,
    overallTotal: 0,
  }

  return {
    profileId: OFFLINE_PROFILE_ID,
    mode: 'ladder',
    exercise,
    ladderState,
    levelCount: cfg.levels.length,
    sessionDefaultSeconds: SESSION_DEFAULT_SECONDS,
    lowBatteryModeSeconds: LOW_BATTERY_MODE_SECONDS,
  }
}

export async function offlineGetNextLadderExercise(
  ladderState: LadderStateDto,
  lastScore: number
): Promise<LadderNextResponseDto> {
  const bundle = await loadLadderOfflineBundle()
  const { allExercises } = await loadCatalogOfflineBundle()
  const config = getConfig(bundle, ladderState.ladderCode)
  if (!config) throw new Error(`Unknown ladder: ${ladderState.ladderCode}`)

  const thresholds = config.thresholds
  const answersNeeded = getAnswersNeededToAdvance(
    ladderState.ladderCode,
    ladderState.currentLevelIndex,
    config
  )
  const maxThreshold = thresholds.answersNeededToAdvance

  let newState: LadderStateDto = {
    ...ladderState,
    recentScores: [...ladderState.recentScores, lastScore].slice(-maxThreshold),
    overallScoreSum: ladderState.overallScoreSum + lastScore,
    overallTotal: ladderState.overallTotal + 1,
  }

  let levelChanged: LadderNextResponseDto['levelChanged'] = null
  const recent = newState.recentScores
  const canEvaluate = recent.length >= answersNeeded

  if (canEvaluate) {
    const avg = recent.reduce((a, b) => a + b, 0) / recent.length
    const maxLevel = Math.max(...config.levels.map((l) => l.levelIndex))

    if (avg >= thresholds.minScoreToAdvance && newState.currentLevelIndex < maxLevel) {
      levelChanged = { from: newState.currentLevelIndex, to: newState.currentLevelIndex + 1, direction: 'up' }
      newState = {
        ...newState,
        currentLevelIndex: newState.currentLevelIndex + 1,
        recentScores: [],
      }
    } else if (avg < thresholds.minScoreToStay && newState.currentLevelIndex > 0) {
      levelChanged = { from: newState.currentLevelIndex, to: newState.currentLevelIndex - 1, direction: 'down' }
      newState = {
        ...newState,
        currentLevelIndex: newState.currentLevelIndex - 1,
        recentScores: [],
      }
    }
  }

  const level =
    config.levels.find((l) => l.levelIndex === newState.currentLevelIndex) ?? null
  if (!level) {
    return { exercise: null, ladderState: newState, levelChanged }
  }

  const picked = pickExerciseForLevel(level, allExercises)
  const exercise = picked ? await hydrateExercise(picked) : null
  return { exercise, ladderState: newState, levelChanged }
}

function averageRecent(s: PerLadderStateDto): number | null {
  if (s.recentScores.length === 0) return null
  return s.recentScores.reduce((a, b) => a + b, 0) / s.recentScores.length
}

const REQUIRED_SCORES_PER_LADDER_WHEN_MANY = 3

export async function offlineStartLadderMixSession(mixCode: string): Promise<LadderMixSessionResponseDto> {
  const bundle = await loadLadderOfflineBundle()
  const { allExercises } = await loadCatalogOfflineBundle()
  const mix = bundle.mixes.find((m) => m.code === mixCode)
  if (!mix || mix.ladderCodes.length < 2) throw new Error(`Ladder mix not found: ${mixCode}`)

  const ladderCodes = mix.ladderCodes
  const perLadderStates: Record<string, PerLadderStateDto> = Object.fromEntries(
    ladderCodes.map((code) => [
      code,
      { recentScores: [], overallScoreSum: 0, overallTotal: 0 } satisfies PerLadderStateDto,
    ])
  )

  const ladderMixState: LadderMixStateDto = {
    mixCode,
    ladderCodes,
    currentLevelIndex: 0,
    perLadderStates,
    nextLadderIndex: 0,
  }

  const firstLadderCode = ladderCodes[0]
  const config = getConfig(bundle, firstLadderCode)
  if (!config) throw new Error(`Ladder config not found: ${firstLadderCode}`)
  const level0 = config.levels.find((l) => l.levelIndex === 0)
  if (!level0) throw new Error(`Ladder ${firstLadderCode} has no level 0`)

  const picked = pickExerciseForLevel(level0, allExercises)
  if (!picked) throw new Error(`No exercise for ladder mix start (${firstLadderCode})`)
  const exercise = await hydrateExercise(picked)

  const levelCount = Math.min(...ladderCodes.map((c) => getConfig(bundle, c)?.levels.length ?? 0))

  return {
    profileId: OFFLINE_PROFILE_ID,
    mode: 'ladderMix',
    exercise,
    ladderMixState,
    levelCount,
    sessionDefaultSeconds: SESSION_DEFAULT_SECONDS,
    lowBatteryModeSeconds: LOW_BATTERY_MODE_SECONDS,
  }
}

export async function offlineGetNextLadderMixExercise(
  state: LadderMixStateDto,
  lastCompletedLadderCode: string,
  lastScore: number
): Promise<LadderMixNextResponseDto> {
  const bundle = await loadLadderOfflineBundle()
  const { allExercises } = await loadCatalogOfflineBundle()

  if (!state.ladderCodes.includes(lastCompletedLadderCode)) {
    throw new Error('Invalid lastCompletedLadderCode')
  }

  const cfgComplete = getConfig(bundle, lastCompletedLadderCode)
  if (!cfgComplete) throw new Error(`Unknown ladder: ${lastCompletedLadderCode}`)
  const thresholds = cfgComplete.thresholds
  const n = state.ladderCodes.length

  const maxThreshold =
    state.ladderCodes.reduce((max, code) => {
      const c = getConfig(bundle, code)
      const t = c?.thresholds.answersNeededToAdvance ?? 5
      return Math.max(max, t)
    }, 0) || 5

  const pls = { ...state.perLadderStates }
  const cur = pls[lastCompletedLadderCode]
  if (cur) {
    pls[lastCompletedLadderCode] = {
      recentScores: [...cur.recentScores, lastScore].slice(-maxThreshold),
      overallScoreSum: cur.overallScoreSum + lastScore,
      overallTotal: cur.overallTotal + 1,
    }
  }
  let newState: LadderMixStateDto = { ...state, perLadderStates: pls }

  let levelChanged: LadderMixNextResponseDto['levelChanged'] = null

  const allHaveEnough = newState.ladderCodes.every((code) => {
    const ladderConfig = getConfig(bundle, code)
    if (!ladderConfig) return false
    const perLevel = getAnswersNeededToAdvance(code, newState.currentLevelIndex, ladderConfig)
    const required = n > 2 ? Math.min(perLevel, REQUIRED_SCORES_PER_LADDER_WHEN_MANY) : perLevel
    return (newState.perLadderStates[code]?.recentScores.length ?? 0) >= required
  })

  if (allHaveEnough) {
    const maxLevel = Math.min(
      ...newState.ladderCodes.map((code) =>
        Math.max(...(getConfig(bundle, code)?.levels.map((l) => l.levelIndex) ?? [0]))
      )
    )

    const passingCount = newState.ladderCodes.filter((code) => {
      const avg = averageRecent(newState.perLadderStates[code]!) ?? 0
      return avg >= thresholds.minScoreToAdvance
    }).length

    const enoughPass = n <= 2 ? passingCount === n : passingCount >= n - 1

    const anyFail = newState.ladderCodes.some((code) => {
      const avg = averageRecent(newState.perLadderStates[code]!) ?? 1
      return avg < thresholds.minScoreToStay
    })

    if (enoughPass && newState.currentLevelIndex < maxLevel) {
      levelChanged = { from: newState.currentLevelIndex, to: newState.currentLevelIndex + 1, direction: 'up' }
      const cleared: Record<string, PerLadderStateDto> = Object.fromEntries(
        newState.ladderCodes.map((code) => [
          code,
          {
            recentScores: [],
            overallScoreSum: newState.perLadderStates[code]!.overallScoreSum,
            overallTotal: newState.perLadderStates[code]!.overallTotal,
          },
        ])
      )
      newState = {
        ...newState,
        currentLevelIndex: newState.currentLevelIndex + 1,
        perLadderStates: cleared,
      }
    } else if (anyFail && newState.currentLevelIndex > 0) {
      levelChanged = { from: newState.currentLevelIndex, to: newState.currentLevelIndex - 1, direction: 'down' }
      const cleared: Record<string, PerLadderStateDto> = Object.fromEntries(
        newState.ladderCodes.map((code) => [
          code,
          {
            recentScores: [],
            overallScoreSum: newState.perLadderStates[code]!.overallScoreSum,
            overallTotal: newState.perLadderStates[code]!.overallTotal,
          },
        ])
      )
      newState = {
        ...newState,
        currentLevelIndex: newState.currentLevelIndex - 1,
        perLadderStates: cleared,
      }
    }
  }

  const nextIdx = (newState.nextLadderIndex + 1) % newState.ladderCodes.length
  newState = { ...newState, nextLadderIndex: nextIdx }
  const nextLadderCode = newState.ladderCodes[newState.nextLadderIndex]
  const nextConfig = getConfig(bundle, nextLadderCode)
  if (!nextConfig) throw new Error(`Unknown ladder: ${nextLadderCode}`)

  const maxIdx = Math.max(...nextConfig.levels.map((l) => l.levelIndex))
  const levelIndex = Math.min(newState.currentLevelIndex, maxIdx)
  const level = nextConfig.levels.find((l) => l.levelIndex === levelIndex)
  if (!level) {
    return { exercise: null, ladderMixState: newState, levelChanged }
  }

  const picked = pickExerciseForLevel(level, allExercises)
  const exercise = picked ? await hydrateExercise(picked) : null
  return { exercise, ladderMixState: newState, levelChanged }
}

export async function offlineListLadders(): Promise<
  { code: string; name: string | null; levelCount: number }[]
> {
  const b = await loadLadderOfflineBundle()
  return b.ladders
}

export async function offlineListLadderMixes(): Promise<
  { code: string; name: string | null; ladderCodes: string[] }[]
> {
  const b = await loadLadderOfflineBundle()
  return b.mixes
}
