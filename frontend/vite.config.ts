import { defineConfig } from 'vitest/config';
import { loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';

export default defineConfig(({mode})=>{
  const env=loadEnv(mode,'.','');
  return {
  plugins: [react(), tailwindcss()],
  server: { port: 5173, proxy: {'/api': {target:env.VITE_API_PROXY_TARGET||'http://localhost:8080',changeOrigin:true,rewrite:(path)=>path.replace(/^\/api/,'')}} },
  test: {
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
    css: true,
  },
  };
});
