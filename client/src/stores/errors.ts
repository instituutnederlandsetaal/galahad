import axios from "axios"

/** API error handling. */
const useErrors = defineStore("errors", () => {
    /** API errors */
    const errors = ref<string[]>([])

    function setupErrorHandler() {
        axios.interceptors.response.use(
            (response) => response,
            async (error) => {
                let message = undefined
                try {
                    message = JSON.parse((await error.response?.data?.text()) ?? "{}").message
                } catch (e) {
                    message = error.response?.data?.message
                }
                errors.value.push(`${error.config?.url}: ${message ?? error.message}`)
                return Promise.reject(error)
            },
        )
    }

    setupErrorHandler()

    return { errors }
})

export default useErrors
