import type { TypeToken } from "@/types/evaluation/distribution"
import * as API from "@/api/evaluation"
import { plausible } from "@/ts/plausible"
import useCorpora from "@/stores/corpora"
import useLayers from "@/stores/layers"

/** Stores and fetches the type token distribution. */
const useDistribution = defineStore("distribution", () => {
    const { hypothesisId, hypothesisLayer } = storeToRefs(useLayers())
    const { corpusId, corpus } = storeToRefs(useCorpora())
    const loading = ref<boolean>(false)
    const distributions = ref<Record<string, TypeToken[]>>()

    function reload(): void {
        if (!corpusId.value || !hypothesisId.value) return
        plausible.distributionEvaluated(corpus.value, hypothesisLayer.value)
        loading.value = true
        API.getDistribution(corpusId.value, hypothesisId.value)
            .then((res) => (distributions.value = res.data))
            .finally(() => (loading.value = false))
    }

    watch(corpusId, () => (distributions.value = undefined))
    watch(hypothesisId, reload)

    return { reload, loading, distributions }
})

export default useDistribution
