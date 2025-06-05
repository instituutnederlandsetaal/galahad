// Libraries & stores

import * as API from "@/api/evaluation"
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
    const distributions = ref<Record<string, DistributionWrapper>>()
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
    const loading = ref(false)
    const posses = computed(() => {
        // A bit hacky using Object.entries, but .map throws on undefined.
        return Object.entries(distribution.value?.distribution)
            ?.map(x => x[1].pos)
            .filter((val, ind, arr) => arr.indexOf(val) === ind) // unique values
            .sort()
    })

    // Methods
    /**
     * Reset it when e.g. the hypothesis or reference is changed.
     */
    function reset(): void {
        distributions.value = undefined
    }

    /**
     * Reloads the term frequency distribution for the given corpus, hypothesis and reference.
     * @param corpus The UUID of the corpus.
     * @param hypothesis The hypothesis job ID.
     */
    function reloadForUUIDHypothesis(corpus: UUID, hypothesis: string): void {
        Utils.reloadEval(
            API.getDistribution,
            reset,
            "fetch distribution",
            loading,
            distributions,
            stores,
            corpus,
            hypothesis,
            ""
        )
    }

    // Exports
    return {
        // Fields
        loading,
        distribution,
        posses,
        selectedDistribution,
        distributionOptions,
        distributions,
        // Methods
        reloadForUUIDHypothesis,
        reset
    }
})

export default useDistribution
