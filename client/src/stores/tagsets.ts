// Libraries & stores

import * as API from "@/api/tagset"
import stores from "@/stores"
// Types & API
import type { Tagset } from "@/types/tagset"

/**
 * Stores all available tagsets. Mainly informational.
 */
const useTagsets = defineStore("tagsets", () => {
    // Stores
    const errorsStore = stores.useErrors()

    // Fields
    const loading = ref(false)
    const tagsets = ref([] as Tagset[])

    // Methods
    function reload() {
        loading.value = true
        API.getTagsets()
            .then(response => (tagsets.value = response.data))
            .catch(error =>
                errorsStore.handleServerError("fetch tagsets", error),
            )
            .finally(() => (loading.value = false))
    }

    reload() // load once

    // Exports
    return {
        loading,
        tagsets,
    }
})

export default useTagsets
