import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'node:path'

// CONCEPT: Vite
// Vite is a build tool + dev server. Unlike older bundlers (Webpack) that
// bundle your ENTIRE app before serving it in development, Vite serves
// source files directly over native ES modules and only compiles what the
// browser actually requests. This makes the dev server start almost
// instantly and hot-reload changes in milliseconds, even in large apps.
export default defineConfig({
  plugins: [react()],
  // The "@" alias mirrors the "paths" entry in tsconfig.json - tsconfig's
  // version only affects TypeScript's type-checker, so Vite/Rollup needs
  // its own copy here to actually resolve "@/..." imports at bundle time.
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 5173,
  },
})
