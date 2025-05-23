// Libraries & stores

import * as API from "@/api/benchmarks"
import stores from "@/stores"
// Types & API
import type { Benchmarks } from "@/types/assays"

/**
 * Contains dataset assays.
 */
const useAssays = defineStore("assays", () => {
    // Stores
    const app = stores.useApp()

    // Fields
    const loading = ref(false)
    const assays = ref({} as Benchmarks)

    // Methods
    /**
     * Reload all assays.
     */
    function reload() {
        loading.value = true
        API.getBenchmarks()
            .then(response => (assays.value = response.data))
            .catch(error => app.handleServerError("fetch assays", error))
            .finally(() => (loading.value = false))
    }

    // Exports
    return {
        // Fields
        assays,
        loading,
        // Methods
        reload,
    }
})

export default useAssays
