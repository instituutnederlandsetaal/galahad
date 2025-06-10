// Libraries & stores

import * as API from "@/api/evaluation"
import { distributionPath } from "@/api/evaluation"
import { useAxios } from "@/api/useAxios"
import stores from "@/stores"
import * as Utils from "@/stores/evaluation/utils"
// API & types
// API & types
import type { UUID } from "@/types/corpora"
import type { DistributionWrapper } from "@/types/evaluation"
import type { SelectOption } from "@/types/ui/select"

// Allows for Object.keys(distribution.table), which dislikes null.
const defaultDistribution = () => ({ distribution: {} }) as DistributionWrapper

/**
 * Stores and fetches the term frequency distribution.
 */
const useDistribution = defineStore("distribution", () => {
    // Fields
    const { hypothesisId } = storeToRefs(stores.useJobSelection())
    const { corpusId } = storeToRefs(stores.useCorpora())
    const url = computed<string>(() => {
        if (!hypothesisId.value) return undefined
        return distributionPath(corpusId.value)
    })

    const { loading, data: distributions } = useAxios<
        Record<string, DistributionWrapper>
    >(url, {}, { hypothesis: hypothesisId.value })

    const distribution = computed(
        () =>
            distributions.value?.[selectedDistribution.value] ??
            defaultDistribution()
    )
    const selectedDistribution = ref<string>()
    const distributionOptions = computed<SelectOption[]>(
        () => {
            if (!distributions.value) {
                return []
            }
            return Object.keys(distributions.value).map(x => ({
                value: x,
                text: x
            }))
        },
        { immediate: true }
    )
    const posses = computed(() => {
        // A bit hacky using Object.entries, but .map throws on undefined.
        return Object.entries(distribution.value?.distribution)
            ?.map(x => x[1].pos)
            .filter((val, ind, arr) => arr.indexOf(val) === ind) // unique values
            .sort()
    })
    // Exports
    return {
        // Fields
        loading,
        distribution,
        posses,
        selectedDistribution,
        distributionOptions,
        distributions
    }
})

export default useDistribution
