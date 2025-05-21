// --- libraries ---

// --- types ---
import type { AxiosError } from "axios"
import type { ErrorMessage } from "@/api/api"

// Custom types

/**
 * Mostly for global error handling.
 */
const useApp = defineStore("app", () => {
    // Fields
    const errors = ref([] as string[])

    // Methods
    function addError(message: string) {
        errors.value.push(message)
    }

    function resetErrors() {
        errors.value = []
    }

    /**
     * Display error in modal.
     * @param intent Human readable explanation.
     * @param error Axios error.
     */
    function handleServerError(intent: string, error: AxiosError<ErrorMessage>) {
        if (error.response) {
            // The request was made and the server responded with a status code
            // that falls out of the range of 2xx
            addError("Failed to " + intent + ":\n" + error?.response?.data?.message)
        } else if (error.request) {
            // The request was made but no response was received
            // `error.request` is an instance of XMLHttpRequest in the browser and an instance of
            // http.ClientRequest in node.js
            // this is a disconnect, it is handled by the user store
        } else {
            // Something happened in setting up the request that triggered an Error
            addError("Failed to " + intent + ":\n" + error.message)
        }
    }

    // Exports
    return { errors, resetErrors, handleServerError }
})

export default useApp
