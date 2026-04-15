/**
 * Absolute URL for files under `public/data/` (Vite `public/` → site root).
 *
 * With `base: './'`, strings like `./data/x.json` are resolved by the browser relative to the
 * **current route path**, so on `/ladder/mix/mix` they become `/ladder/data/x.json` → 404.
 * Use root-relative `/data/...` when `base` is `./` (Capacitor / nested SPA routes).
 */
export function bundledDataUrl(filename: string): string {
  const name = filename.replace(/^\//, '')
  const base = import.meta.env.BASE_URL
  if (base === './') {
    return `/data/${name}`
  }
  const prefix = base.endsWith('/') ? base : `${base}/`
  return `${prefix}data/${name}`
}
