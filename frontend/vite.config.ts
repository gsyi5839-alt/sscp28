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
