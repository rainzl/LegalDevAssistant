import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// Dev: proxy /api → Spring Boot. Production: same origin — UI is copied into the JAR (classpath:/static/) when you `mvn package` from `backend/`.
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
