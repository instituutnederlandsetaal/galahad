import * as API from "@/api/evaluation/comparison"
import { plausible } from "@/ts/plausible"
import useCorpora from "@/stores/corpora"
import useLayers from "@/stores/layers"
import type { TermComparison } from "@/types/evaluation"

/** Stores and fetches the type token distribution. */
const useComparison = defineStore("comparison", () => {
    const { hypothesisId, referenceId, hypothesisLayer, referenceLayer } = storeToRefs(useLayers())
    const { corpusId, corpus } = storeToRefs(useCorpora())
    const loading = ref<boolean>(false)
    const matches = ref<TermComparison[]>()
    const annotations = ref<string[]>()
    const document = ref<string>()

    function reload(): void {
        if ([corpusId.value, hypothesisId.value, referenceId.value, document.value].includes(undefined)) return
        loading.value = true
        API.getDocumentLayerComparison(corpusId.value, hypothesisId.value, document.value, referenceId.value)
            .then((res) => (matches.value = res.data))
            .then(() => {
                // plausible todo
            })
            .finally(() => (loading.value = false))
    }

    watch([corpusId, hypothesisId, referenceId], () => {
        matches.value = undefined
        annotations.value = undefined
        document.value = undefined
    })
    watch(document, reload)

    return { reload, loading, matches, annotations, document }
})

export default useComparison
