<template>
    <GCard title="Document View">
        <template #help>
            The document view show the differences between the reference and hypothesis layer in a single document. Red
            words indicate a difference between the layers for the selected annotation. Hover over a word to see all
            annotations.
        </template>

        <p v-if="loading">Loading...</p>

        <GForm v-if="bothJobsSelected">
            <DocumentSelect v-model="selectedDoc" />
            <AnnotationSelect v-if="annotationOptions" v-model="selectedAnnotation" :options="annotationOptions" />
        </GForm>

        <div v-if="selectedDoc && selectedAnnotation" class="document">
            <template v-for="tc in termcomps.slice(firstRecord, firstRecord + rowsToDisplay)" :key="tc.ref.id">
                <span class="wordComparison" :class="{ incorrect: !annotationsEqual(tc) }">
                    {{ tc.ref.annotations["token"] }}
                    <SingleTermComparisonTable :hypoTerm="tc.hyp" :refTerm="tc.ref" class="tooltip" />
                </span>

                <!-- add newline after . -->
                <br v-if="tc.ref.annotations['token'] == '.'" />
            </template>

            <Paginator
                v-if="termcomps.length > rowsToDisplay"
                v-model:first="firstRecord"
                :rows="rowsToDisplay"
                :totalRecords="termcomps.length"
            ></Paginator>
        </div>
        <p v-else>Select a reference layer, a hypothesis layer. Then, select a document and an annotation.</p>
    </GCard>
</template>

<script setup lang="ts">
// Library & API

import * as API from "@/api/evaluation"
import stores from "@/stores"
// Types & Stores
import type { Term, TermComparison } from "@/types/evaluation"
import type { SelectOption } from "@/types/ui/select"

import Paginator from "primevue/paginator"

// Stores
const documentsStore = stores.useDocuments()
const corporaStore = stores.useCorpora()
const jobSelection = stores.useJobSelection()

// Fields
const selectedDoc = ref<string>()
const selectedAnnotation = ref<string>()
const annotationOptions = ref<SelectOption[]>()
const termcomps = ref<TermComparison[]>(null)
const loading = ref<boolean>()
/** Paginator */
const firstRecord = ref<number>(0)
const rowsToDisplay = ref<number>(200)
const bothJobsSelected = computed(() => {
    return jobSelection.hypothesisId && jobSelection.referenceId
})

// Watches & mounts
watch(selectedDoc, async (newVal) => {
    if (newVal) {
        loading.value = true
        API.getDocumentLayerComparison(
            corporaStore.corpusId,
            jobSelection.hypothesisId,
            newVal,
            jobSelection.referenceId,
        )
            .then((response) => {
                termcomps.value = response.data
            })
            .finally(() => {
                loading.value = false
            })
    }
})

watch(termcomps, () => {
    if (!termcomps.value) return
    annotationOptions.value = documentsStore.documents
        .find((doc) => doc.name === selectedDoc.value)
        ?.annotations.map((key) => ({ value: key, text: key }))
})

// Methods
function annotationsEqual(tc: TermComparison) {
    const refAnnot = cleanAnnotation(tc.ref)
    const hypoAnnot = cleanAnnotation(tc.hyp)
    return refAnnot === hypoAnnot
}

function cleanAnnotation(term: Term) {
    return term.annotations[selectedAnnotation.value]?.toLowerCase().replace("_", "")
}
</script>

<style scoped lang="scss">
.document {
    padding: 2rem;
}

.tooltip {
    min-width: fit-content;
    min-height: fit-content;
}

.wordComparison {
    display: inline-block;
    position: relative;
    margin: 0;
    padding: 0.2rem;
    text-align: center;
    font-size: 0.8rem;
    line-height: 0.8rem;
}

.incorrect {
    background-color: rgba(255, 0, 0, 0.1);
}

.wordComparison .tooltip {
    visibility: hidden;
    position: absolute;
    z-index: 1;
    width: max-content;
    bottom: 100%;
    text-align: left;
    padding: 0;
    margin: 0;
}

.wordComparison:hover .tooltip {
    visibility: visible;
}

.table-controls {
    display: flex;
    justify-content: center;
    gap: 1rem;
}
</style>
