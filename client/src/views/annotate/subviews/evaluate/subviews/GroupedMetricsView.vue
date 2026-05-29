<template>
    <div>
        <MetricsTable
            title="Grouped Metrics"
            :loading
            :columns
            :items
            @download="(data) => download(data)"
            :downloading
        >
            <template #help>
                <p>
                    In Grouped Metrics an overview is given of the (dis)agreement for lemma and PoS per part-of-speech.
                    For each PoS, different metrics are given by choosing the annotation and the grouping. By clicking
                    on a percentage, a data sample is shown.
                </p>
            </template>
            <template #header>
                <GSelect :options="groupingOptions" v-model="selectedGrouping" />
                <p>
                    <b> Only the 100 most frequent groups are shown. </b>
                </p>
                <!-- <MetricsFilter ref="metricsFilter" :annotations="metrics.metrics" /> -->
            </template>
        </MetricsTable>
    </div>
</template>

<script setup lang="ts">
// Libraries & stores
import * as API from "@/api/evaluation"
import * as Utils from "@/api/utils"
// API & types
import useMetrics, { metricsPerPosColumns } from "@/stores/evaluation/metrics"
import type MetricsFilter from "@/components/tables/MetricsFilter.vue"
import useLayers from "@/stores/layers"
import useCorpora from "@/stores/corpora"

// Stores
const { loading, metrics } = storeToRefs(useMetrics())
const corporaStore = useCorpora()
const jobSelection = useLayers()

// Fields
const downloading = ref<boolean>()
const selectedGrouping = ref<string>("lemmaByLemma")
const groupingOptions = computed(() => Object.keys(metrics.value || {}).map((key) => ({ value: key, text: key })))

const columns = computed(() => metricsPerPosColumns)
const metricsFilter = useTemplateRef<InstanceType<typeof MetricsFilter>>("metricsFilter")
const metricName = computed(() => {
    return metricsFilter.value?.metricName
})

const posMetrics = computed(() => {
    if (metrics.value?.[selectedGrouping.value] == null) return []
    console.log(metrics.value[selectedGrouping.value])
    // Copy over the metrics (depending on selectedMetric.value) from:
    // { ADJ: { ADJ: { pos : { f1, recall, ... }, lemma : { f1, recall, ... } } } } }
    // to:
    // { ADJ: { ADJ: { f1, recall, ..., } } }
    const ret = Object.entries(metrics.value[selectedGrouping.value]?.grouped || {}).map(([name, i]) => ({
        column: selectedGrouping.value.split("By")[1].toLowerCase(),
        name: name,
        count: i.count,
        truePositive: i.truePositive,
        falsePositive: i.falsePositive,
        falseNegative: i.falseNegative,
        noMatch: i.noMatch,
        precision: i.metrics.precision,
        recall: i.metrics.recall,
        f1: i.metrics.f1,
    }))
    return ret
})
const singlePosMetrics = computed(() => {
    return Object.values(posMetrics.value).filter((pos) => !pos.name.includes("+"))
})
const multiPosMetrics = computed(() => {
    return Object.values(posMetrics.value).filter((pos) => pos.name.includes("+"))
})
const items = computed(() => {
    // if (selectedSingleOrMultiple.value == "single") return singlePosMetrics.value
    // if (selectedSingleOrMultiple.value == "multi") return multiPosMetrics.value
    return posMetrics.value
})

// Methods
function download(data: Any) {
    const classType = data.column.key
    const group = data.item.name

    downloading.value = true
    API.getMetricsSamples(
        corporaStore.corpusId,
        jobSelection.hypothesisId,
        jobSelection.referenceId,
        metricName.value,
        classType,
        group,
    )
        .then((response) => {
            Utils.browserDownloadResponseFile(response)
        })
        .finally(() => (downloading.value = false))
}
</script>
