import stores from "@/stores"
import type { TypeToken } from "@/types/evaluation/distribution"
import * as API from "@/api/evaluation"
import { plausible } from "@/ts/plausible"

/** Stores and fetches the type token distribution. */
const useDistribution = defineStore("distribution", () => {
    const { hypothesisId, hypothesisJob } = storeToRefs(stores.useJobSelection())
    const { corpusId, corpus } = storeToRefs(stores.useCorpora())
    const loading = ref<boolean>(false)
    const distributions = ref<Record<string, TypeToken[]>>()

    function reload(): void {
        if (!corpusId.value || !hypothesisId.value) return
        plausible.distributionEvaluated(corpus.value, hypothesisJob.value)
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
