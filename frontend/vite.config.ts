import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    // Auto import Element Plus components and styles
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
    {
      name: 'inject-build-meta',
      transformIndexHtml(html) {
        const stamp = new Date().toISOString()
        return html.replace(
          '<head>',
          `<head>\n    <meta name="frontend-build" content="${stamp}" />`
        )
      },
    },
  ],
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
    // Output to frontend/dist uniformly here; then safely sync to site root directory through npm script.
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    minify: 'esbuild',
    emptyOutDir: true,
    // Target modern mobile browsers for smaller output
    target: 'es2020',
    // Element Plus must stay package-level to avoid circular initialization across internal chunks.
    chunkSizeWarningLimit: 500,
    cssCodeSplit: true,
    rollupOptions: {
      output: {
        // Split vendor chunks for better caching on mobile
        manualChunks(id) {
          if (id.includes('node_modules')) {
            // Keep Element Plus package internals together to preserve initialization order.
            if (id.includes('@element-plus/icons-vue')) return 'vendor-element-icons'
            if (id.includes('element-plus')) return 'vendor-element'
            if (id.includes('dayjs')) return 'vendor-date'
            if (id.includes('lodash-es')) return 'vendor-lodash'
            // Vue core libraries
            if (id.includes('vue') || id.includes('pinia') || id.includes('vue-router')) return 'vendor-vue'
            // All other node_modules
            return 'vendor-misc'
          }
          // Game module (composables + constants + game components)
          if (id.includes('/views/game/')) return 'game-module'
        },
        // Use hashed filenames for long-term caching
        chunkFileNames: 'assets/js/[name]-[hash].js',
        entryFileNames: 'assets/js/[name]-[hash].js',
        assetFileNames: 'assets/[ext]/[name]-[hash].[ext]',
      }
    }
  }
})
