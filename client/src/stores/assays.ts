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
    const errors = stores.useErrors()

    // Fields
    const loading = ref<boolean>()
    const assays = ref<Benchmarks>()

    // Methods
    /**
     * Reload all assays.
     */
    function reload(): void {
        loading.value = true
        API.getBenchmarks()
            .then(response => (assays.value = response.data))
            .catch(error => errors.handle(error))
            .finally(() => (loading.value = false))
    }

    // Exports
    return {
        // Fields
        assays,
        loading,
        // Methods
        reload
    }
})

export default useAssays
