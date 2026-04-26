/**
 * Regenerates public/data/catalog-offline.json from a running API (subjects + all exercises).
 * Start backend first: ./gradlew bootRun --args='--spring.profiles.active=local'
 * Usage (from frontend/): node scripts/export-catalog-offline.mjs [baseUrl]
 * Default baseUrl: http://localhost:5173/api
 */
import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const root = path.join(__dirname, '..')

const base = (process.argv[2] ?? 'http://localhost:5173/api').replace(/\/$/, '')

const NBACK_ID_TO_LEVEL = {
  'c0000000-0000-0000-0000-000000000001': 1,
  'c0000000-0000-0000-0000-000000000002': 2,
  'c0000000-0000-0000-0000-000000000003': 3,
}

async function fetchJson(url) {
  const res = await fetch(url)
  if (!res.ok) throw new Error(`${url} -> ${res.status}`)
  return res.json()
}

function seqLen(ex) {
  const p = ex.nBackParams ?? ex.nbackParams
  return p?.sequence?.length ?? 0
}

async function main() {
  const subjects = await fetchJson(`${base}/subjects`)
  let allExercises = await fetchJson(`${base}/exercises`)

  const outList = []
  for (const ex of allExercises) {
    if (ex.type === 'N_BACK' && seqLen(ex) === 0) {
      const level = NBACK_ID_TO_LEVEL[ex.id]
      if (level) {
        const nback = await fetchJson(`${base}/nback/${level}`)
        const p = nback.nBackParams ?? nback.nbackParams
        if (p?.sequence?.length) {
          outList.push({ ...ex, nBackParams: p, nbackParams: p })
          continue
        }
      }
    }
    outList.push(ex)
  }

  const bundle = {
    subjects,
    allExercises: outList,
  }
  const out = path.join(root, 'public', 'data', 'catalog-offline.json')
  fs.mkdirSync(path.dirname(out), { recursive: true })
  fs.writeFileSync(out, JSON.stringify(bundle, null, 2))
  console.log(`Wrote ${out} (${outList.length} exercises, ${subjects.length} subjects)`)
}

main().catch((e) => {
  console.error(e)
  process.exit(1)
})
