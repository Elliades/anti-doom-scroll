import type { ExerciseDto } from '../types/api'
import { bundledDataUrl } from '../utils/bundledDataUrl'
import type { SubjectDto } from './subjects'

/** Snapshot from `public/data/catalog-offline.json` — used when `VITE_DATA_MODE=local`. */
export interface CatalogOfflineBundle {
  subjects: SubjectDto[]
  allExercises: ExerciseDto[]
}

const useBundledCatalog = import.meta.env.VITE_DATA_MODE === 'local'

let bundleCache: CatalogOfflineBundle | null = null
const exerciseById = new Map<string, ExerciseDto>()

export function isCatalogOfflineMode(): boolean {
  return useBundledCatalog
}

export async function loadCatalogOfflineBundle(): Promise<CatalogOfflineBundle> {
  if (!useBundledCatalog) {
    throw new Error('Catalog offline bundle is only used when VITE_DATA_MODE=local')
  }
  if (bundleCache) return bundleCache
  const url = bundledDataUrl('catalog-offline.json')
  const res = await fetch(url)
  if (!res.ok) {
    throw new Error(
      `Missing bundled catalog (${res.status}). Run \`npm run export:catalog-offline\` with the backend up, then rebuild the app.`
    )
  }
  bundleCache = (await res.json()) as CatalogOfflineBundle
  exerciseById.clear()
  for (const ex of bundleCache.allExercises) {
    exerciseById.set(ex.id, ex)
  }
  return bundleCache
}

export async function getOfflineExerciseById(id: string): Promise<ExerciseDto | null> {
  await loadCatalogOfflineBundle()
  return exerciseById.get(id) ?? null
}
