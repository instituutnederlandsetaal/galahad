// Libraries & stores

import { metricsPath } from "@/api/evaluation"
import * as Utils from "@/stores/evaluation/utils"
// API & types
import type { UUID } from "@/types/corpora"
import { useAxios } from "@/api/useAxios"

import type { Metrics, MetricsRow } from "@/types/evaluation"
import { plausible } from "@/ts/plausible"
import useLayers from "@/stores/layers"
import useCorpora from "@/stores/corpora"

export const metricsPerPosColumns = [
    {
        key: "group",
        sortOn: (x: MetricsRow) => (Number.isNaN(Number.parseInt(x.name)) ? x.name : Number.parseInt(x.name)),
    },
    // { key: 'accuracy', sortOn: (x: MetricsRow) => x.accuracy },
    { key: "precision", align: "right", sortOn: (x: MetricsRow) => x.precision },
    { key: "recall", align: "right", sortOn: (x: MetricsRow) => x.recall },
    { key: "f1", align: "right", sortOn: (x: MetricsRow) => x.f1 },
    { key: "count", align: "right", sortOn: (x: MetricsRow) => x.count },
    {
        key: "truePositive",
        label: "true positive",
        button: true,
        sortOn: (x: MetricsRow): number => x.truePositive.count,
    },
    {
        key: "falsePositive",
        label: "false positive",
        button: true,
        sortOn: (x: MetricsRow): number => x.falsePositive.count,
    },
    {
        key: "falseNegative",
        label: "false negative",
        button: true,
        sortOn: (x: MetricsRow): number => x.falseNegative.count,
    },
    { key: "noMatch", label: "no match", button: true, sortOn: (x: MetricsRow): number => x.noMatch.count },
]

/**
 * Stores and fetches the Lemma & PoS accuracy metrics.
 */
const useMetrics = defineStore("metrics", () => {
    const { hypothesisId, referenceId, hypothesisLayer, referenceLayer } = storeToRefs(useLayers())
    const { corpusId, corpus } = storeToRefs(useCorpora())
    const url = computed<string>(() => {
        if (!(hypothesisId.value && referenceId.value)) return undefined
        plausible.metricsEvaluated(corpus.value, hypothesisLayer.value, referenceLayer.value)
        return metricsPath(corpusId.value, hypothesisId.value)
    })
    const { loading, data: metrics } = useAxios<Metrics>(
        url,
        {},
        { hypothesis: hypothesisId.value, reference: referenceId.value },
    )

    return { metrics, loading }
})

export default useMetrics
