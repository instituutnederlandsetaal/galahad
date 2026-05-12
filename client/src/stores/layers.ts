import * as API from "@/api/layers"
import stores from "@/stores"
import type { LayerMetadata } from "@/types/layers"

/** Contains the layers for the current corpus. */
const useLayers = defineStore("layers", () => {
    // Stores
    const { corpusId } = storeToRefs(stores.useCorpora())

    // Fields
    const loading = ref<boolean>()
    const layers = ref<LayerMetadata[]>([])

    /** Reload layers */
    function reload(): void {
        if (!corpusId.value) return
        loading.value = true
        API.getLayers(corpusId.value)
            .then((res) => (layers.value = res.data))
            .finally(() => (loading.value = false))
    }

    watch(corpusId, reload)

    reload()

    // Exports
    return {
        // Fields
        layers,
        loading,
        // Methods
        reload,
    }
})

export default useLayers
