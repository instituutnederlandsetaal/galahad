// Libraries & stores

import * as API from "@/api/taggers"
import stores from "@/stores"
// API & types
import type { Tagger } from "@/types/taggers"

/**
 * Sort the 'annotations' field of the taggers. The order is stochastic when retrieved from the API.
 * For the interface, we want the order to be fixed.
 */
export function sort_tagger_annotations(types: string[]): string[] {
    // By pure coincidence, reverse sorting makes the order TOK, POS, LEM, which is acceptable.
    // But we might want a different order at some point.
    return types.sort((a, b) => b.localeCompare(a))
}

/**
 * Stores all available taggers. Mainly informational.
 */
const useTaggers = defineStore("taggers", () => {
    // Stores
    const errorsStore = stores.useErrors()

    // Fields
    const loading = ref(false)
    const taggers = ref([] as Tagger[])

    // Methods
    function reload() {
        loading.value = true
        API.getTaggers()
            .then(response => (taggers.value = response.data))
            .catch(error => {
                errorsStore.handleServerError("fetch taggers", error)
            })
            .finally(() => (loading.value = false))
    }

    reload() // load once

    // Exports
    return {
        loading,
        taggers,
    }
})

export default useTaggers
