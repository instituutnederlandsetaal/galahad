import axios from "axios"

/** API error handling. */
const useErrors = defineStore("errors", () => {
    /** API errors */
    const errors = ref<string[]>([])

    function setupErrorHandler() {
        axios.interceptors.response.use(
            (response) => response,
            (error) => {
                errors.value.push(`${error.config?.url}: ${error.response?.data?.message ?? error.message}`)
                return Promise.reject(error)
            },
        )
    }

    setupErrorHandler()

    return { errors }
})

export default useErrors
