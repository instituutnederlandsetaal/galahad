import stores from "@/stores"
import type { TypeToken } from "@/types/evaluation/distribution"
import * as API from "@/api/evaluation"

/** Stores and fetches the type token distribution. */
const useDistribution = defineStore("distribution", () => {
    const { hypothesisId } = storeToRefs(stores.useJobSelection())
    const { corpusId } = storeToRefs(stores.useCorpora())
    const loading = ref<boolean>(false)
    const distributions = ref<Record<string, TypeToken[]>>()

    function reload(): void {
        if (!corpusId.value || !hypothesisId.value) return
        loading.value = true
        API.getDistribution(corpusId.value, hypothesisId.value)
            .then((res) => (distributions.value = res.data))
            .finally(() => (loading.value = false))
    }

    return {
        reload,
        loading,
        distributions,
    }
})

export default useDistribution
