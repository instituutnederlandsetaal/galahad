import type { TypeToken } from "@/types/evaluation/distribution"
import * as API from "@/api/evaluation"
import { plausible } from "@/ts/plausible"
import useCorpora from "@/stores/corpora"
import useLayers from "@/stores/layers"

/** Stores and fetches the type token distribution. */
const useGroupedMetrics = defineStore("groupedMetrics", () => {
    const { hypothesisId, referenceId, hypothesisLayer, referenceLayer } = storeToRefs(useLayers())
    const { corpusId, corpus } = storeToRefs(useCorpora())
    const loading = ref<boolean>(false)
    const groupedMetrics = ref<any>()
    const annotation = ref<string>()
    const group = ref<string>()

    function reload(): void {
        if ([corpusId.value, hypothesisId.value, referenceId.value, annotation.value, group.value].includes(undefined))
            return
        plausible.metricsEvaluated(corpus.value, hypothesisLayer.value, referenceLayer.value)
        loading.value = true
        API.getGroupedMetrics(corpusId.value, hypothesisId.value, referenceId.value, annotation.value, group.value)
            .then((res) => (groupedMetrics.value = res.data))
            .finally(() => (loading.value = false))
    }

    watch([corpusId, hypothesisId, referenceId], () => {
        groupedMetrics.value = undefined
        annotation.value = undefined
        group.value = undefined
    })
    watch([annotation, group], reload)

    return { reload, loading, groupedMetrics, annotation, group }
})

export default useGroupedMetrics
