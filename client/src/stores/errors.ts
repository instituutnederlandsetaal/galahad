import axios from "axios"

/** API error handling. */
const useErrors = defineStore("errors", () => {
    /** API errors */
    const errors = ref<string[]>([])

    function setupErrorHandler() {
        axios.interceptors.response.use(
            (response) => response,
            async (error) => {
                const message = JSON.parse(await error.response?.data?.text() ?? "{}").message;
                errors.value.push(`${error.config?.url}: ${message ?? error.message}`)
                return Promise.reject(error)
            },
        )
    }

    setupErrorHandler()

    return { errors }
})

export default useErrors
