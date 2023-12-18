import { defineConfig } from 'vite';

export default defineConfig({
  build: {
    lib: {
      name: 'appsec-kit-plugin',
      entry: 'frontend/appsec-kit/appsec-kit-plugin.ts',
      formats: ['es'],
    },
    outDir: 'target/classes/META-INF/frontend/appsec-kit',
    rollupOptions: {
      external: [
        /^lit.*/,
        /^Frontend\/generated\/jar-resources.*/,
      ]
    },
  },
  esbuild: {
    tsconfigRaw: {
      compilerOptions: {
        experimentalDecorators: true,
      }
    }
  },
});
