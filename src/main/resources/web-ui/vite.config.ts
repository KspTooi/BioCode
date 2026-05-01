import { fileURLToPath, URL } from "node:url";
import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import vueDevTools from "vite-plugin-vue-devtools";
import icons from "unplugin-icons/vite";
import IconsResolver from "unplugin-icons/resolver";
import Components from "unplugin-vue-components/vite";
import checker from "vite-plugin-checker";

// https://vite.dev/config/
export default defineConfig(({ command }) => ({
  // 设置静态资源路径
  base: "/",

  plugins: [
    vue(),
    vueDevTools(),
    ...(command === "serve"
      ? [
          checker({
            vueTsc: true,
            eslint: {
              useFlatConfig: true,
              lintCommand: 'eslint "./src/**/*.{ts,tsx,vue,js,jsx}"',
              dev: {
                logLevel: ["error"],
              },
            },
            overlay: {
              initialIsOpen: true,
              position: "tr", // 悬浮框位置：Top Right
            },
          }),
        ]
      : []),
    // (monacoEditorPlugin as any).default({
    //   languageWorkers: ['editorWorkerService', 'css', 'html', 'json', 'typescript']
    // }),
    Components({
      resolvers: [IconsResolver()],
    }),
    icons({
      autoInstall: true,
      compiler: "vue3",
      customCollections: {
        // 如果需要自定义图标集可以在这里配置
      },
    }),
  ],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  server: {
    host: "0.0.0.0",
    port: 27501,
    cors: {
      origin: "*",
      methods: ["GET", "POST", "PUT", "DELETE", "OPTIONS"],
      allowedHeaders: ["Content-Type", "Authorization", "X-Requested-With"],
    },
    proxy: {
      // 代理 API 请求到后端服务
      "/api": {
        target: "http://127.0.0.1:27500", // 后端服务地址
        changeOrigin: true, // 修改请求源为目标 URL
        rewrite: (path) => path.replace(/^\/api/, ""), // 移除 /api 前缀
        ws: true, // 支持 WebSocket
        secure: false, // 如果是 https 接口，需要配置这个参数
      },
    },
  },
  build: {
    outDir: "dist",
    assetsDir: "assets",
    rollupOptions: {
      input: {
        admin: fileURLToPath(new URL("./index.html", import.meta.url)),
      },
      output: {
        entryFileNames: "assets/[name].js",
        chunkFileNames: "assets/[name].js",
        assetFileNames: (assetInfo) => {
          const name = assetInfo.name || "";
          if (name.endsWith(".css")) {
            return "assets/[name].css";
          }
          return "assets/[name].[ext]";
        },
      },
    },
  },
}));
