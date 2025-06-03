import type { ErrorMessage } from "@/api"
import type { AxiosError } from "axios"

/**
 * Mostly for global error handling.
 */
const useErrors = defineStore("errors", () => {
    const errors = ref<string[]>([])

    function addError(message: string): void {
        errors.value.push(message)
    }

    function reset(): void {
        errors.value = []
    }

    /**
     * Display error in modal.
     * @param intent Human readable explanation.
     * @param error Axios error.
     */
    function handle(intent: string, error: AxiosError<ErrorMessage>): void {
        if (error.response) {
            // The request was made and the server responded with a status code
            // that falls out of the range of 2xx
            addError(`Failed to ${intent}:\n${error?.response?.data?.message}`)
        } else if (error.request) {
            // The request was made but no response was received
            // `error.request` is an instance of XMLHttpRequest in the browser and an instance of
            // http.ClientRequest in node.js
            // this is a disconnect, it is handled by the user store
        } else {
            // Something happened in setting up the request that triggered an Error
            addError(`Failed to ${intent}:\n${error.message}`)
        }
    }

    // Exports
    return { errors, reset, handle }
})

export default useErrors
