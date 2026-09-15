import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';

const apiTarget = process.env.CARDDEMO_API ?? 'http://localhost:3000';

/** The Express API in `../src/online/api.ts` is served from these paths. */
const apiPaths = ['/signon', '/signoff', '/menu', '/accounts'];

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: Object.fromEntries(
      apiPaths.map((path) => [path, { target: apiTarget, changeOrigin: true }]),
    ),
  },
});
