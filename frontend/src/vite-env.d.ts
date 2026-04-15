/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_URL?: string
  /** `local` = bundled `public/data/*.json` for standalone / no backend. */
  readonly VITE_DATA_MODE?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
