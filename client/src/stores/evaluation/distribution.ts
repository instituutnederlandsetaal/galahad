// Libraries & stores

import * as API from "@/api/evaluation"
import stores from "@/stores"
import * as Utils from "@/stores/evaluation/utils"
// API & types
// API & types
import type {UUID} from "@/types/corpora"
import type {DistributionWrapper} from "@/types/evaluation"

// Allows for Object.keys(distribution.table), which dislikes null.
const defaultDistribution = () => ({distribution: {}}) as DistributionWrapper

/**
 * Stores and fetches the term frequency distribution.
 */
const useDistribution = defineStore("distribution", () => {
    // Fields
    const distributions = ref({} as Record<string, DistributionWrapper>)
    const distribution = computed(
        () =>
            distributions.value[selectedDistribution.value] ??
            defaultDistribution(),
    )
    const selectedDistribution = ref(null)
    const distributionOptions = computed(() =>
        Object.keys(distributions.value).map(x => ({value: x, text: x})),
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
    function reset() {
        distribution.value = defaultDistribution()
    }

    /**
     * Reloads the term frequency distribution for the given corpus, hypothesis and reference.
     * @param corpus The UUID of the corpus.
     * @param hypothesis The hypothesis job ID.
     */
    function reloadForUUIDHypothesis(corpus: UUID, hypothesis: string) {
        Utils.reloadEval(
            API.getDistribution,
            reset,
            "fetch distribution",
            loading,
            distributions,
            stores,
            corpus,
            hypothesis,
            "",
        )
        selectedDistribution.value = null
    }

    // Exports
    return {
        // Fields
        loading,
        distribution,
        posses,
        selectedDistribution,
        distributionOptions,
        // Methods
        reloadForUUIDHypothesis,
        reset,
    }
})

export default useDistribution
