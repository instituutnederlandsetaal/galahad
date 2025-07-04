import { confusionPath } from "@/api/evaluation"
import { useAxios } from "@/api/useAxios"
import stores from "@/stores"
import type { ConfusionWrapper } from "@/types/evaluation"

/** Stores and fetches the confusion matrix. */
const useConfusion = defineStore("confusion", () => {
    const { hypothesisId, referenceId } = storeToRefs(stores.useJobSelection())
    const { corpusId } = storeToRefs(stores.useCorpora())
    const url = computed<string>(() => {
        if (!(hypothesisId.value && referenceId.value)) return undefined
        return confusionPath(corpusId.value)
    })
    const { loading, data: confusion } = useAxios<ConfusionWrapper>(
        url,
        {},
        { hypothesis: hypothesisId.value, reference: referenceId.value },
    )

    return { confusion, loading }
})

export default useConfusion
