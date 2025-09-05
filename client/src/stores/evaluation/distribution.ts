import { distributionPath } from "@/api/evaluation"
import { useAxios } from "@/api/useAxios"
import stores from "@/stores"
import type { TypeToken, Distribution } from "@/types/evaluation/distribution"
import type { SelectOption } from "@/types/ui/select"

/** Stores and fetches the term frequency distribution. */
const useDistribution = defineStore("distribution", () => {
    // Fields
    const { hypothesisId } = storeToRefs(stores.useJobSelection())
    const { corpusId } = storeToRefs(stores.useCorpora())
    const url = computed<string>(() => {
        if (!hypothesisId.value) return undefined
        return distributionPath(corpusId.value)
    })

    const { loading, data: distributions } = useAxios<Record<string, Distribution>>(url, undefined, {
        hypothesis: hypothesisId.value,
    })

    const distribution = computed<TypeToken[]>(() => distributions.value?.[selectedDistribution.value])
    const selectedDistribution = ref<string>()
    const distributionOptions = computed<SelectOption[]>(() =>
        Object.keys(distributions.value || {}).map((x) => ({ value: x, text: x })),
    )
    const posses = computed<string[]>(() =>
        [...new Set(Object.values(distribution.value || {}).map((t: TypeToken) => t.group))].sort(),
    )
    // Exports
    return {
        // Fields
        loading,
        distribution,
        posses,
        selectedDistribution,
        distributionOptions,
        distributions,
    }
})

export default useDistribution
