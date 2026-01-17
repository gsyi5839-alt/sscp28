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
    // 重要：outDir 必须与 root/publicDir 分离，且不能指向 root 的父目录，否则 Vite 会警告有覆盖源码风险
    // 这里统一输出到 frontend/dist；再通过 npm script 安全同步到站点根目录。
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    minify: 'esbuild',
    // dist 目录可以放心清空
    emptyOutDir: true
  }
})
