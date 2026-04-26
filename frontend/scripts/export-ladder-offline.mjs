/**
 * Regenerates public/data/ladder-offline.json from GET /api/ladders/offline-bundle.
 * Start backend first: ./gradlew bootRun --args='--spring.profiles.active=local'
 * Usage (from frontend/): node scripts/export-ladder-offline.mjs [baseUrl]
 * Default baseUrl: http://localhost:5173/api
 */
import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const root = path.join(__dirname, '..')

const base = (process.argv[2] ?? 'http://localhost:5173/api').replace(/\/$/, '')

async function main() {
  const url = `${base}/ladders/offline-bundle`
  const res = await fetch(url)
  if (!res.ok) {
    throw new Error(`GET ${url} -> ${res.status}`)
  }
  const data = await res.json()
  const out = path.join(root, 'public', 'data', 'ladder-offline.json')
  fs.mkdirSync(path.dirname(out), { recursive: true })
  fs.writeFileSync(out, JSON.stringify(data, null, 2))
  const n = Object.keys(data.configs ?? {}).length
  console.log(`Wrote ${out} (${data.ladders?.length ?? 0} ladders, ${data.mixes?.length ?? 0} mixes, ${n} configs)`)
}

main().catch((e) => {
  console.error(e)
  process.exit(1)
})
