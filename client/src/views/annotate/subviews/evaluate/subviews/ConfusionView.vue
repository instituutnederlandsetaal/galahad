<template>
    <GCard>
        <GTable
            title="Confusion table"
            helpLink="evaluation"
            :columns
            :items="rows"
            :loading
            sortColumn="referenceJob"
            class="confusion"
        >
            <template #help>
                <p>
                    In part-of-speech confusion, an overview is given of the matches (in green) and mismatches per PoS
                    when comparing the tagging of the hypothesis layer with the reference layer. Click on any frequency
                    below to show a data sample.
                </p>
                <p>
                    The category "MULTIPLE" contains combined tags like "ADP+NOU" or "VRB+PD+PD". These are shown in one
                    cell, but this does not mean that the taggers agree on the exact tags. Click on the cell or look at
                    the Global Metrics for more details.
                </p>
                <DifferentTagsetsHelp />
            </template>

            <template #header>
                <GForm v-if="confusion">
                    <fieldset>
                        <label for="annotation-select">Annotation</label>
                        <GSelect id="annotation-select" :options="annotationOptions" v-model="selectedAnnotation" />
                    </fieldset>
                </GForm>
            </template>

            <template #table-empty>
                Select a reference layer and a hypothesis layer to generate a confusion table.
            </template>

            <!-- top left header -->
            <template #head-referenceJob>
                part-of-speech <br />
                ({{ jobSelection.hypothesisId }}→)<br />
                ({{ jobSelection.referenceId }}↓)
            </template>

            <!-- custom cell rendering -->
            <template #cell="data: Cell">
                <!-- header column -->
                <span v-if="data.column.key == 'referenceJob'">
                    {{ data.value }}
                </span>
                <GButton v-else :disabled="!data.value" :class="cssClass(data)" @click="openModal(data)">
                    {{ data.value ? data.value.count : 0 }}
                </GButton>
            </template>
        </GTable>

        <ComparisonModal
            v-if="samples"
            @hide="samples = undefined"
            :samples
            :downloading
            @download="(data) => download(data)"
            :referenceJob="jobSelection.referenceId"
            :hypothesisJob="jobSelection.hypothesisId"
        />
    </GCard>
</template>

<script setup lang="ts">
import stores from "@/stores"
import * as API from "@/api/evaluation"
import * as Utils from "@/api/utils"
import { Confusion, type EvaluationEntry, type Samples } from "@/types/evaluation"
import type { Column } from "@/types/ui/table"

// Stores
const { loading, confusions } = storeToRefs(stores.useConfusion())
const corporaStore = stores.useCorpora()
const jobSelection = stores.useJobSelection()
const { hypothesisId, referenceId } = storeToRefs(stores.useJobSelection())
const { reload } = stores.useConfusion()
watch([hypothesisId, referenceId], reload, { immediate: true })

// Custom types
type Item = { [key: string]: EvaluationEntry } & { referenceJob: string }
type Cell = { field: Column; item: Item; value: EvaluationEntry }

// Fields
// Selected confusion
const annotationOptions = computed(() => Object.keys(confusions.value ?? {}).map((key) => ({ value: key, text: key })))
watch(annotationOptions, () => (selectedAnnotation.value = annotationOptions.value[0]?.value))
const selectedAnnotation = ref<string>()
const confusion = computed<Confusion>(() => confusions.value?.[selectedAnnotation.value])

const downloading = ref<boolean>()
const modalData = ref({})
const samples = ref<Samples>()

const columns = computed((): Column[] => {
    if (!confusion.value) return []
    // add the entries
    const entries = {} as { [key: string]: boolean }
    Object.keys(confusion.value)?.map((k1) => {
        Object.keys(confusion.value[k1])?.forEach((k2) => (entries[k2] = true))
    })

    // add referenceJob, sort, map and return
    const refJobField = {
        key: "referenceJob",
        sortOn: (value: Item) => {
            const pos = value.referenceJob
            return posToBottom(pos) ? Number.POSITIVE_INFINITY : pos
        },
    }

    const allFields = Object.keys(entries)
        // GTable sort also uses localeCompare. Just using sort() as is messes up the order
        // between e.g. 'NOU' & 'NO_POS'. I don't know why, though.
        .sort((a, b) => {
            if (posToBottom(a)) return 1
            if (posToBottom(b)) return -1
            return a.localeCompare(b)
        })

    const returnVal = allFields.map((field) => {
        return { key: field, sortOn: (value) => (field !== "referenceJob" ? value[field]?.count : value?.referenceJob) }
    })
    returnVal.unshift(refJobField)
    return returnVal
})

const rows = computed((): Item[] => {
    if (!confusion.value) return []
    return Object.keys(confusion.value).map((k1) => {
        const ret = { referenceJob: k1 } as { [key: string]: EvaluationEntry } & { referenceJob: string }
        Object.keys(confusion.value[k1]).forEach((k2) => (ret[k2] = confusion.value[k1][k2]))
        return ret
    })
})

// Methods
function download() {
    const data = modalData.value
    const hypothesisPos = data.column.key
    const referencePos = data.item.referenceJob
    downloading.value = true
    API.getConfusionSamples(
        corporaStore.corpusId,
        jobSelection.hypothesisId,
        jobSelection.referenceId,
        hypothesisPos,
        referencePos,
        selectedAnnotation.value,
    )
        .then((response) => {
            Utils.browserDownloadResponseFile(response)
        })
        .finally(() => (downloading.value = false))
}
/** Case insensitive string compare. */
function strEqual(a: string, b: string) {
    return a.toUpperCase() === b.toUpperCase()
}

/** returns whether this pos should be sorted to the bottom. */
function posToBottom(pos: string) {
    const posses = ["NO_POS", "Missing match", "OTHER", "LET", "PUNCT", "PC", "MULTIPLE"]
    return posses.includes(pos)
}

function cssClass(data) {
    const match: boolean = strEqual(data.column.key, data.item.referenceJob)
    const warnings = ["NO_POS", "MULTIPLE", "Missing match"]
    if (warnings.includes(data.column.key)) {
        return { orange: match, plain: !match }
    }
    return { green: match, plain: !match }
}

function openModal(data) {
    modalData.value = data
    samples.value = {
        agreement: strEqual(data.column.key, data.item.referenceJob),
        samples: data.value.samples,
        hypothesisPos: data.column.key,
        referencePos: data.item.referenceJob,
        annotationType: selectedAnnotation.value,
    }
}
</script>

<style scoped lang="scss">
:deep(.confusion) td {
    padding: 0 !important;
    span {
        padding: 0 0.5rem !important;
    }
    button {
        width: 100%;
        border: 0;

        &.plain {
            background-color: transparent;

            &:hover {
                background-color: var(--int-light-grey) !important;
            }

            &:focus {
                background-color: var(--int-light-grey-hover) !important;
            }
        }
    }
}

/*
#confusionTable :deep(td) {
    padding: 0 !important;
    margin: 0;
}

#confusionTable :deep(.table-control) {
    min-height: auto;
}

#confusionTable td {
    button {
        display: block;
        text-align: center;
        width: 100%;
        height: 100%;
        margin: 0;

        &.plain {
            background-color: transparent;

            &:hover {
                background-color: var(--int-light-grey) !important;
            }

            &:focus {
                background-color: var(--int-light-grey-hover) !important;
            }
        }
    }
}
    */
</style>
