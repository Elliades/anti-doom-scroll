import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
/** Shared Vite options for dev, build, and Vitest (no PWA — see `vite.config.ts`). */
export default defineConfig({
    base: './',
    plugins: [react()],
    server: {
        port: 5174, // Backend runs on 5173; frontend dev on 5174
        proxy: {
            '/api': {
                target: 'http://localhost:5173',
                changeOrigin: true,
            },
        },
    },
});
