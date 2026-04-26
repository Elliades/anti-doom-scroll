import { mergeConfig } from 'vite'
import { defineConfig } from 'vitest/config'
import viteConfigBase from './vite.config.base'

export default mergeConfig(
  viteConfigBase,
  defineConfig({
    test: {
      environment: 'happy-dom',
      setupFiles: ['./src/test/setup.ts'],
      globals: true
    }
  })
)
