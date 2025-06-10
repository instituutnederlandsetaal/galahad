<template>
    <GCard>
        <div class="table-controls" v-if="bothJobsSelected">
            <div class="table-control">
                <label for="annotation-select">Annotation:</label>
                <GSelect id="annotation-select" :options="confusionableAnnotations" v-model="selectedAnnotation" />
            </div>
        </div>
        <GTable title="Part-of-speech confusion" helpLink="evaluation" :columns :items="rows" id="confusionTable"
            :loading sortColumn="referenceJob" :sortDesc="false">
            <template #help>
                <p>
                    In part-of-speech confusion, an overview is given of the matches (in green) and mismatches per PoS
                    when
                    comparing the tagging of the hypothesis layer with the reference layer. Click on any frequency below
                    to
                    show a data sample.
                </p>
                <p>
                    The category "MULTIPLE" contains combined tags like "ADP+NOU" or "VRB+PD+PD". These are shown in one
                    cell, but this does not mean that the taggers agree on the exact tags. Click on the cell or look at
                    the
                    Global Metrics for more details.
                </p>
                <DifferentTagsetsHelp />
            </template>

            <template #header>

            </template>

            <template #table-empty>Select a reference layer, a hypothesis layer and an annotation to
                generate a confusion table.</template>

            <!-- top left header -->
            <template #head-referenceJob>
                part-of-speech <br />
                ({{ jobSelection.hypothesisId }}→)<br />
                ({{ jobSelection.referenceId }}↓)
            </template>

            <!-- custom cell rendering -->
            <template #cell="data: Cell">
                <!-- header column -->
                <div v-if="data.column.key == 'referenceJob'">
                    {{ data.value }}
                </div>

                <!-- cell -->
                <GButton v-else :disabled="!data.value.count" :class="cssClass(data)" @click="openModal(data)">
                    {{ `${(data.value ? data.value.count : 0).toString().padStart(3, "&nbsp;")}` }}
                </GButton>
            </template>
        </GTable>

        <ComparisonModal v-if="samples" @hide="samples = undefined" :samples :downloading
            @download="(data) => download(data)" :referenceJob="jobSelection.referenceId"
            :hypothesisJob="jobSelection.hypothesisId" />
    </GCard>
</template>

<script setup lang="ts">
// Libraries & stores

import stores from "@/stores"

import * as API from "@/api/evaluation"
import * as Utils from "@/api/utils"
import type { EvaluationEntry, Samples } from "@/types/evaluation"
// API & types
import type { Column } from "@/types/ui/table"

// Stores
const { loading, confusion } = storeToRefs(stores.useConfusion())
const corporaStore = stores.useCorpora()
const jobSelection = stores.useJobSelection()
const errors = stores.useErrors()

// Custom types
type Item = { [key: string]: EvaluationEntry } & { referenceJob: string }
type Cell = { field: Column; item: Item; value: EvaluationEntry }

// Fields
const confusionableAnnotations = computed(() =>
    Object.keys(confusion.value || {}).map(key => ({ value: key, text: key }))
)
const selectedAnnotation = ref<string>()
const downloading = ref<boolean>()
const modalData = ref({})
const samples = ref<Samples>()
const selectedConfusion = computed(
    () => confusion?.value[selectedAnnotation.value] || { table: {} }
)
const bothJobsSelected = computed(() => {
    return jobSelection.hypothesisId && jobSelection.referenceId
})

const columns = computed((): Column[] => {
    // add the entries
    const entries = {} as { [key: string]: boolean }
    Object.keys(selectedConfusion?.value?.table)?.map(k1 => {
        Object.keys(selectedConfusion?.value?.table[k1])?.forEach(
            k2 => (entries[k2] = true)
        )
    })

    // add referenceJob, sort, map and return
    const refJobField = {
        key: "referenceJob",
        sortOn: (value: Item) => {
            const pos = value.referenceJob
            return posToBottom(pos) ? Number.POSITIVE_INFINITY : pos
        }
    }

    const allFields = Object.keys(entries)
        // GTable sort also uses localeCompare. Just using sort() as is messes up the order
        // between e.g. 'NOU' & 'NO_POS'. I don't know why, though.
        .sort((a, b) => {
            if (posToBottom(a)) return 1
            if (posToBottom(b)) return -1
            return a.localeCompare(b)
        })

    const returnVal = allFields.map(field => {
        return {
            key: field,
            sortOn: value =>
                field !== "referenceJob"
                    ? value[field]?.count
                    : value?.referenceJob
        }
    })
    returnVal.unshift(refJobField)
    return returnVal
})

const rows = computed((): Item[] => {
    return Object.keys(selectedConfusion.value.table).map(k1 => {
        const ret = { referenceJob: k1 } as {
            [key: string]: EvaluationEntry
        } & {
            referenceJob: string
        }
        Object.keys(selectedConfusion.value.table[k1]).forEach(
            k2 => (ret[k2] = selectedConfusion.value.table[k1][k2])
        )
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
        selectedAnnotation.value
    )
        .then(response => {
            Utils.browserDownloadResponseFile(response)
        })
        .catch(res =>
            Utils.handleBlobError(res, "download confusion samples", errors)
        )
        .finally(() => (downloading.value = false))
}
/**
 * Case insensitive string compare.
 */
function strEqual(a: string, b: string) {
    return a.toUpperCase() === b.toUpperCase()
}

/**
 * returns whether this pos should be sorted to the bottom.
 */
function posToBottom(pos: string) {
    const posses = [
        "NO_POS",
        "Missing match",
        "OTHER",
        "LET",
        "PUNCT",
        "PC",
        "MULTIPLE"
    ]
    return posses.includes(pos)
}

function cssClass(data) {
    const match: boolean = strEqual(data.column.key, data.item.referenceJob)
    const warnings = ["NO_POS", "MULTIPLE", "Missing match"]
    if (warnings.includes(data.column.key)) {
        return {
            orange: match,
            plain: !match
        }
    }
    return {
        green: match,
        plain: !match
    }
}

function openModal(data) {
    modalData.value = data
    samples.value = {
        agreement: strEqual(data.column.key, data.item.referenceJob),
        samples: data.value.samples,
        hypothesisPos: data.column.key,
        referencePos: data.item.referenceJob,
        annotationType: selectedAnnotation.value
    }
}
</script>

<style scoped lang="scss">
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
</style>
