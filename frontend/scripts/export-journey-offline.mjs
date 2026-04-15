/**
 * Regenerates public/data/journey-offline.json from a running API.
 * Start backend first: ./gradlew bootRun --args='--spring.profiles.active=local'
 * Usage (from frontend/): node scripts/export-journey-offline.mjs [baseUrl]
 * Default baseUrl: http://localhost:5173/api
 */
import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const root = path.join(__dirname, '..')

const base = (process.argv[2] ?? 'http://localhost:5173/api').replace(/\/$/, '')

async function main() {
  const journey = await (await fetch(`${base}/journey?code=default`)).json()
  const s0 = await (await fetch(`${base}/journey/steps/0/content?journeyCode=default`)).json()
  const s1 = await (await fetch(`${base}/journey/steps/1/content?journeyCode=default`)).json()
  const s2a = await (await fetch(`${base}/journey/steps/2/content?journeyCode=default&chapterIndex=0`)).json()
  const s2b = await (await fetch(`${base}/journey/steps/2/content?journeyCode=default&chapterIndex=1`)).json()
  const bundle = {
    journey,
    stepContents: {
      0: s0,
      1: s1,
      '2-0': s2a,
      '2-1': s2b,
    },
  }
  const out = path.join(root, 'public', 'data', 'journey-offline.json')
  fs.mkdirSync(path.dirname(out), { recursive: true })
  fs.writeFileSync(out, JSON.stringify(bundle, null, 2))
  console.log(`Wrote ${out}`)
}

main().catch((e) => {
  console.error(e)
  process.exit(1)
})
