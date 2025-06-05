import type { ErrorMessage } from "@/api"
import type { AxiosError } from "axios"

/** API error handling. */
const useErrors = defineStore("errors", () => {
    /** API errors */
    const errors = ref<string[]>([])

    /** Empty errors list */
    function reset(): void {
        errors.value = []
    }

    /** Adds to errors list. Should display error modal. */
    function handle(error: AxiosError<ErrorMessage>): void {
        if (error.response) {
            // Request was made and server responded with status code outside of 2xx.
            errors.value.push(error?.response?.data?.message)
        } else {
            // No response received. Request error.
            errors.value.push(error.message)
        }
    }

    return { errors, reset, handle }
})

export default useErrors
