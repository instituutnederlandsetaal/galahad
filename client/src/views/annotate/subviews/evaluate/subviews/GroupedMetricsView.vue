<template>
    <GCard>
        <MetricsTable
            v-model="tableData"
            title="Grouped Metrics"
            :loading
            :columns
            :items="filteredItems"
            @download="(data) => download(data)"
            :downloading
            sortColumn="hypCount"
        >
            <template #help>
                <p>
                    In Grouped Metrics an overview is given of the (dis)agreement for lemma and PoS per part-of-speech.
                    For each PoS, different metrics are given by choosing the annotation and the grouping. By clicking
                    on a percentage, a data sample is shown.
                </p>
            </template>
            <template #header>
                <template v-if="commonAnnotations.length">
                    <GForm>
                        <fieldset>
                            <label for="annotation-select">Annotation</label>
                            <AnnotationSelect
                                id="annotation-select"
                                :options="annotationOptions"
                                v-model="selectedAnnotation"
                            />
                        </fieldset>
                        <fieldset>
                            <label for="group-select">Group by</label>
                            <AnnotationSelect id="group-select" :options="groupOptions" v-model="selectedGroup" />
                        </fieldset>
                        <fieldset v-if="groupedMetrics">
                            <label for="analysis-select">Single/multiple analyses</label>
                            <GSelect id="analysis-select" :options="analysesOptions" v-model="selectedAnalysis" />
                        </fieldset>
                    </GForm>
                    <aside>
                        <p>Micro summary:</p>
                        <AnnotationSummary :annotations="groupedMetrics?.micro ?? {}" />
                    </aside>
                    <aside>
                        <p>Macro summary:</p>
                        <AnnotationSummary :annotations="groupedMetrics?.macro ?? {}" />
                    </aside>
                </template>
                <p v-else>Select a reference layer and a hypothesis layer</p>
            </template>
        </MetricsTable>

        <ComparisonModal
            v-if="tableData"
            :evaluationEntry="tableData.value"
            :hypothesisLayer
            :referenceLayer
            :annotations="[selectedAnnotation, selectedGroup]"
            :downloading
            @download="() => download(tableData)"
            @hide="tableData = undefined"
        >
            <template #title>
                Samples of {{ referenceId }} <i>{{ tableData.item.referenceAnnotation }}</i> and {{ hypothesisId }}
                {{ tableData.column.key }}</template
            >
        </ComparisonModal>
    </GCard>
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
import type { SelectOption } from "@/types/ui/select"
import useGroupedMetrics from "@/stores/evaluation/groupedMetrics"
import type { Column, TableData } from "@/types/ui/table"
import type { MetricsRow } from "@/types/evaluation"

const { commonAnnotations, hypothesisId, referenceId, hypothesisLayer, referenceLayer } = storeToRefs(useLayers())
const {
    loading,
    groupedMetrics,
    annotation: selectedAnnotation,
    group: selectedGroup,
} = storeToRefs(useGroupedMetrics())
const { corpusId } = storeToRefs(useCorpora())

const tableData = ref()

// Form
const annotationOptions = computed(() =>
    // only logical annotations TODO might filter, might not
    commonAnnotations.value.filter((option: SelectOption) => !["token"].includes(option.text)),
)
const groupOptions = computed(() =>
    // only logical groups
    commonAnnotations.value.filter((option: SelectOption) => !["head"].includes(option.text)),
)
const analysesOptions: SelectOption[] = [
    { value: "both", text: "Both" },
    { value: "single", text: "Single" },
    { value: "multiple", text: "Multiple" },
]
const selectedAnalysis = ref<string>(analysesOptions[0].value)

// Table data
const columns = computed((): Column<MetricsRow>[] => [
    {
        key: "group",
        label: selectedGroup.value,
        sortOn: (x: MetricsRow) => (Number.isNaN(Number.parseInt(x.name)) ? x.name : Number.parseInt(x.name)),
    },
    { key: "accuracy", label: `${selectedAnnotation.value}<br>accuracy`, sortOn: (x: MetricsRow) => x.accuracy },
    { key: "precision", label: `${selectedAnnotation.value}<br>precision`, sortOn: (x: MetricsRow) => x.precision },
    { key: "recall", label: `${selectedAnnotation.value}<br>recall`, sortOn: (x: MetricsRow) => x.recall },
    { key: "f1", label: `${selectedAnnotation.value}<br>f1`, sortOn: (x: MetricsRow) => x.f1 },
    { key: "hypCount", label: "count<br>(hypothesis)", align: "right", sortOn: (x: MetricsRow) => x.hypCount },
    { key: "refCount", label: "count<br>(reference)", align: "right", sortOn: (x: MetricsRow) => x.refCount },
    {
        key: "truePositive",
        label: `${selectedAnnotation.value}<br>true positive`,
        button: true,
        sortOn: (x: MetricsRow): number => x.truePositive.count,
    },
    {
        key: "falsePositive",
        label: `${selectedAnnotation.value}<br>false positive`,
        button: true,
        sortOn: (x: MetricsRow): number => x.falsePositive.count,
    },
    {
        key: "falseNegative",
        label: `${selectedAnnotation.value}<br>false negative`,
        button: true,
        sortOn: (x: MetricsRow): number => x.falseNegative.count,
    },
    { key: "noMatch", label: "no match", button: true, sortOn: (x: MetricsRow): number => x.noMatch.count },
])
const grouped = computed(() => {
    if (!groupedMetrics.value) return []
    return groupedMetrics.value.grouped
})
const items = computed(() => {
    if (!grouped.value) return []
    return Object.entries(grouped.value).map((entry) => ({
        ...entry[1],
        group: entry[0],
        precision: entry[1].metrics.precision,
        recall: entry[1].metrics.recall,
        f1: entry[1].metrics.f1,
        accuracy: entry[1].metrics.accuracy,
    }))
})
const filteredItems = computed(() => {
    if (selectedAnalysis.value === "single") {
        return items.value.filter((i) => !i.group.includes("+"))
    }
    if (selectedAnalysis.value === "multiple") {
        return items.value.filter((i) => i.group.includes("+"))
    }
    return items.value
})

// Methods
const downloading = ref<boolean>(false)
function download(data: TableData<any>) {
    const classification = data.column.key
    const groupFilter = data.item.group
    downloading.value = true
    API.getMetricsSamples(
        // TODO plausible
        corpusId.value,
        hypothesisId.value,
        referenceId.value,
        selectedAnnotation.value,
        selectedGroup.value,
        classification,
        groupFilter,
    )
        .then((response) => {
            Utils.browserDownloadResponseFile(response)
        })
        .finally(() => (downloading.value = false))
}

// Default select options
watchPostEffect(() => {
    selectedAnnotation.value = annotationOptions.value[0]?.value
})
watchPostEffect(() => {
    selectedGroup.value = groupOptions.value[2]?.value
})
</script>

<style scoped lang="scss">
aside {
    text-align: center;
}
</style>
