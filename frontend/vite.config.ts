import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    host: '0.0.0.0',
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    // Important: outDir must be separate from root/publicDir and cannot point to parent directory of root, otherwise Vite will warn about risk of overwriting source code
    // Output to frontend/dist uniformly here; then safely sync to site root directory through npm script.
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    minify: 'esbuild',
    // dist directory can be safely cleared
    emptyOutDir: true
  }
})
