import stores from "@/stores"
import type { Confusion } from "@/types/evaluation"
import * as API from "@/api/evaluation"
import { plausible } from "@/ts/plausible"

/** Stores and fetches the confusion matrix. */
const useConfusion = defineStore("confusion", () => {
    const { hypothesisId, referenceId, hypothesisJob, referenceJob } = storeToRefs(stores.useJobSelection())
    const { corpusId, corpus } = storeToRefs(stores.useCorpora())
    const loading = ref<boolean>(false)
    const confusions = ref<Record<string, Confusion>>()

    function reload(): void {
        if (!corpusId.value || !hypothesisId.value || !referenceId.value) return
        plausible.confusionEvaluated(corpus.value, hypothesisJob.value, referenceJob.value)
        loading.value = true
        API.getConfusion(corpusId.value, hypothesisId.value, referenceId.value)
            .then((res) => (confusions.value = res.data))
            .finally(() => (loading.value = false))
    }

    return { reload, confusions, loading }
})

export default useConfusion
