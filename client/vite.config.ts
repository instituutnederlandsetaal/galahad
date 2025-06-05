import { URL, fileURLToPath } from "node:url"

import yaml from "@rollup/plugin-yaml"
import vue from "@vitejs/plugin-vue"
import AutoImport from "unplugin-auto-import/vite"
import Components from "unplugin-vue-components/vite"
import { defineConfig } from "vite"
import { nodePolyfills } from "vite-plugin-node-polyfills"
import vueDevTools from "vite-plugin-vue-devtools"

// https://vite.dev/config/
export default defineConfig({
    plugins: [
        vue(),
        vueDevTools(),
        yaml(),
        nodePolyfills({ include: ["buffer", "path"] }),
        AutoImport({
            imports: ["vue", "pinia", "vue-router", "@vueuse/core"],
            dts: true,
        }),
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
