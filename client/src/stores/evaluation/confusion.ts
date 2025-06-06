import { confusionPath } from "@/api/evaluation"
import { useAxios } from "@/api/useAxios"
import stores from "@/stores"
import type { ConfusionWrapper } from "@/types/evaluation"

/** Stores and fetches the confusion matrix. */
const useConfusion = defineStore("confusion", () => {
    const { hypothesisId: hypothesis, referenceId: reference } = storeToRefs(
        stores.useJobSelection()
    )
    const { activeUUID } = storeToRefs(stores.useCorpora())
    const url = computed<string>(() => {
        if (!(hypothesis.value && reference.value)) return undefined
        return confusionPath(activeUUID.value)
    })
    const { loading, data: confusion } = useAxios<ConfusionWrapper>(
        url,
        {},
        { hypothesis: hypothesis.value, reference: reference.value }
    )

    return {
        confusion,
        loading
    }
})

export default useConfusion
