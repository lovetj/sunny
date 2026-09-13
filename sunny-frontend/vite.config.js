import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

export default defineConfig({
  plugins: [uni()],
  server: {
    port: 5173,
    host: '0.0.0.0',
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/sunny_file': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      // 高德 Web 服务 API 代理 (H5 开发时解决浏览器跨域问题)
      '/amap': {
        target: 'https://restapi.amap.com',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/amap/, '')
      }
    }
  }
})
