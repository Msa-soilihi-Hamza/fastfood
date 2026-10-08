import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    // En développement, /api est redirigé vers Spring Boot.
    // Le navigateur croit parler au même serveur : on retire l'en-tête Origin pour que
    // la vérification CORS de l'API ne dépende pas du port choisi par Vite (5173, 5174…).
    proxy: {
      '/api': {
        target: process.env.API_URL ?? 'http://localhost:8080',
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq) => proxyReq.removeHeader('origin'))
        },
      },
    },
  },
})
