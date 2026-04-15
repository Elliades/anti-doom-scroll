import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const root = path.join(path.dirname(fileURLToPath(import.meta.url)), '..')
const dest = path.join(root, '.env.android')
const src = path.join(root, '.env.android.example')
if (!fs.existsSync(dest)) {
  fs.copyFileSync(src, dest)
  console.warn(
    '[ensure-android-env] Created .env.android from .env.android.example — edit if you need a custom API URL.'
  )
}
