import Aura from "@primeuix/themes/aura"
import PrimeVue from "primevue/config"
import App from "@/App.vue"
import router from "@/router"
import "@/assets/main.scss"

import { setAxiosBaseUrl } from "@/api"
setAxiosBaseUrl()

const app = createApp(App)

app.use(createPinia())
app.use(PrimeVue, {
    theme: {
        preset: Aura,
        options: {
            darkModeSelector: ".my-app-dark",
        },
    },
})

app.use(router)
app.mount("#app")
