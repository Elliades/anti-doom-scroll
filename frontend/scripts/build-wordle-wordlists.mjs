/**
 * Builds Wordle word lists for frontend (split by length) and Spring Boot (flat JSON).
 * Normalization matches Kotlin WordleGenerator.normalizeWordleWord and TS normalizeForCompare
 * (NFD → strip combining marks → lowercase → œ→oe æ→ae).
 *
 * Sources: see scripts/wordlist-sources/SOURCES.md
 * Usage (from frontend/): npm run build:wordle-words
 */
import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const cacheDir = path.join(__dirname, 'wordlist-sources')
const frontendWordleData = path.join(__dirname, '..', 'src', 'data', 'wordle_words')
const backendWords = path.join(__dirname, '..', '..', 'src', 'main', 'resources', 'words')

const MIN_LEN = 3
const MAX_LEN = 7

const URLS = {
  en: {
    file: 'words_alpha.txt',
    url: 'https://raw.githubusercontent.com/dwyl/english-words/master/words_alpha.txt',
  },
  fr: {
    file: 'fr_50k.txt',
    url: 'https://raw.githubusercontent.com/hermitdave/FrequencyWords/master/content/2018/fr/fr_50k.txt',
  },
}

/** Align with Kotlin: NFD, strip M marks, lower, œ/æ */
function normalizeWordleWord(raw) {
  const nfd = raw.normalize('NFD')
  const noMarks = nfd.replace(/\p{M}+/gu, '')
  return noMarks.toLowerCase().replace(/œ/g, 'oe').replace(/æ/g, 'ae')
}

function isAlphaAscii(s) {
  return /^[a-z]+$/.test(s)
}

async function ensureCached({ file, url }) {
  const dest = path.join(cacheDir, file)
  if (fs.existsSync(dest)) {
    const st = fs.statSync(dest)
    if (st.size > 0) return dest
  }
  fs.mkdirSync(cacheDir, { recursive: true })
  process.stderr.write(`Downloading ${url}\n`)
  const res = await fetch(url)
  if (!res.ok) throw new Error(`GET ${url} -> ${res.status}`)
  const buf = Buffer.from(await res.arrayBuffer())
  fs.writeFileSync(dest, buf)
  return dest
}

function buildEnglishSet(filePath) {
  const text = fs.readFileSync(filePath, 'utf8')
  const set = new Set()
  for (const line of text.split(/\r?\n/)) {
    const w = line.trim().toLowerCase()
    if (!w || !/^[a-z]+$/.test(w)) continue
    const len = w.length
    if (len < MIN_LEN || len > MAX_LEN) continue
    set.add(w)
  }
  return set
}

function buildFrenchSet(filePath) {
  const text = fs.readFileSync(filePath, 'utf8')
  const set = new Set()
  for (const line of text.split(/\r?\n/)) {
    if (!line.trim()) continue
    const rawWord = line.split(/\s+/)[0] ?? ''
    const w = normalizeWordleWord(rawWord)
    if (!w || !isAlphaAscii(w)) continue
    const len = w.length
    if (len < MIN_LEN || len > MAX_LEN) continue
    set.add(w)
  }
  return set
}

function writeJsonSorted(outPath, wordSet) {
  const arr = [...wordSet].sort((a, b) => a.localeCompare(b))
  fs.mkdirSync(path.dirname(outPath), { recursive: true })
  fs.writeFileSync(outPath, JSON.stringify(arr), 'utf8')
  return arr.length
}

/** Split flat set into per-length maps for frontend chunks. */
function wordsByLength(wordSet) {
  /** @type {Record<number, Set<string>>} */
  const buckets = {}
  for (let L = MIN_LEN; L <= MAX_LEN; L++) buckets[L] = new Set()
  for (const w of wordSet) {
    const L = w.length
    if (L >= MIN_LEN && L <= MAX_LEN) buckets[L].add(w)
  }
  return buckets
}

function writeFrontendSplit(lang, wordSet) {
  const buckets = wordsByLength(wordSet)
  const dir = path.join(frontendWordleData, lang)
  let total = 0
  for (let L = MIN_LEN; L <= MAX_LEN; L++) {
    const n = writeJsonSorted(path.join(dir, `${L}.json`), buckets[L])
    total += n
  }
  return total
}

async function main() {
  const enPath = await ensureCached(URLS.en)
  const frPath = await ensureCached(URLS.fr)

  const enSet = buildEnglishSet(enPath)
  const frSet = buildFrenchSet(frPath)

  const enFlatCount = writeJsonSorted(path.join(backendWords, 'wordle_en.json'), enSet)
  const frFlatCount = writeJsonSorted(path.join(backendWords, 'wordle_fr.json'), frSet)

  const enSplitTotal = writeFrontendSplit('en', enSet)
  const frSplitTotal = writeFrontendSplit('fr', frSet)

  process.stdout.write(
    `Backend (flat): wordle_en.json ${enFlatCount} words, wordle_fr.json ${frFlatCount} words\n` +
      `Frontend (split ${MIN_LEN}–${MAX_LEN}): en/ ${enSplitTotal} total, fr/ ${frSplitTotal} total\n` +
      `Wrote: ${frontendWordleData} and ${backendWords}\n`
  )
}

main().catch((e) => {
  console.error(e)
  process.exit(1)
})
