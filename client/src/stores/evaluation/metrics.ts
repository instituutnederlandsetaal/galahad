// Libraries & stores

import { metricsPath } from "@/api/evaluation"
import stores from "@/stores"
import * as Utils from "@/stores/evaluation/utils"
// API & types
import type { UUID } from "@/types/corpora"
import { useAxios } from "@/api/useAxios"

import type { Metrics, MetricsRow } from "@/types/evaluation"
import { plausible } from "@/ts/plausible"

export const metricsPerPosColumns = [
    {
        key: "name",
        label: "group",
        sortOn: (x: MetricsRow) => (Number.isNaN(Number.parseInt(x.name)) ? x.name : Number.parseInt(x.name)),
    },
    // { key: 'accuracy', sortOn: (x: MetricsRow) => x.accuracy },
    { key: "precision", sortOn: (x: MetricsRow) => x.precision },
    { key: "recall", sortOn: (x: MetricsRow) => x.recall },
    { key: "f1", sortOn: (x: MetricsRow) => x.f1 },
    { key: "count", label: "count", sortOn: (x: MetricsRow) => x.count },
    { key: "truePositive", label: "true positive", sortOn: (x: MetricsRow): number => x.truePositive.count / x.count },
    {
        key: "falsePositive",
        label: "false positive",
        sortOn: (x: MetricsRow): number => x.falsePositive.count / x.count,
    },
    {
        key: "falseNegative",
        label: "false negative",
        sortOn: (x: MetricsRow): number => x.falseNegative.count / x.count,
    },
    { key: "noMatch", label: "no match", sortOn: (x: MetricsRow): number => x.noMatch.count / x.count },
]

/**
 * Stores and fetches the Lemma & PoS accuracy metrics.
 */
const useMetrics = defineStore("metrics", () => {
    const { hypothesisId, referenceId, hypothesisJob, referenceJob } = storeToRefs(stores.useJobSelection())
    const { corpusId, corpus } = storeToRefs(stores.useCorpora())
    const url = computed<string>(() => {
        if (!(hypothesisId.value && referenceId.value)) return undefined
        plausible.metricsEvaluated(corpus.value, hypothesisJob.value, referenceJob.value)
        return metricsPath(corpusId.value)
    })
    const { loading, data: metrics } = useAxios<Metrics>(
        url,
        {},
        { hypothesis: hypothesisId.value, reference: referenceId.value },
    )

    return { metrics, loading }
})

export default useMetrics
