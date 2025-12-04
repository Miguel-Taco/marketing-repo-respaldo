import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  // Configuración añadida para el servidor de desarrollo
  server: {
    port: 5600, // Puerto fijo para la aplicación pública de encuestas
  },
})