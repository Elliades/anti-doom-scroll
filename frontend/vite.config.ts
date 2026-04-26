import { mergeConfig, defineConfig } from 'vite'
import { VitePWA } from 'vite-plugin-pwa'
import base from './vite.config.base'

export default mergeConfig(
  base,
  defineConfig({
    plugins: [
      VitePWA({
        registerType: 'autoUpdate',
        manifest: {
          name: 'Anti-Doom Scroll',
          short_name: 'AntiDoom',
          description: 'Replace doomscroll with micro-exercises',
          theme_color: '#1a1a2e',
          background_color: '#16213e',
          display: 'standalone',
          start_url: '/',
          icons: [
            {
              src: '/icon-192.png',
              sizes: '192x192',
              type: 'image/png',
              purpose: 'any maskable',
            },
            {
              src: '/icon-512.png',
              sizes: '512x512',
              type: 'image/png',
              purpose: 'any maskable',
            },
          ],
        },
        workbox: {
          globPatterns: ['**/*.{js,css,html,ico,png,svg,woff2,json}'],
        },
      }),
    ],
  })
)
