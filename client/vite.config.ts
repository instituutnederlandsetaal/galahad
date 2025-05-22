import { fileURLToPath, URL } from "node:url"

import { defineConfig } from "vite"
import vue from "@vitejs/plugin-vue"
import vueDevTools from "vite-plugin-vue-devtools"
import yaml from "@rollup/plugin-yaml"
import { nodePolyfills } from "vite-plugin-node-polyfills"
import AutoImport from "unplugin-auto-import/vite"
import Components from "unplugin-vue-components/vite"

// https://vite.dev/config/
export default defineConfig({
    plugins: [
        vue(),
        vueDevTools(),
        yaml(),
        nodePolyfills({ include: ["buffer", "path"] }),
        AutoImport({ imports: ["vue", "pinia", "vue-router"], dts: true }),
        Components({ dts: true }),
    ],
    server: {
        watch: {
            usePolling: true,
        },
    },
    resolve: {
        alias: {
            "@": fileURLToPath(new URL("./src", import.meta.url)),
        },
    },
})
