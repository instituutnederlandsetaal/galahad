<template>
    <GCard title="Document View">

        <template #help>
            The document view show the differences between the reference and hypothesis layer in a single
            document. Red words indicate a difference between the layers for the selected annotation. Hover over a word
            to see all annotations.
        </template>

        <p v-if="loading">Loading...</p>

        <div class="table-controls">
            <div class="table-control">
                Document:
                <GInput type="select" :options="docNames" v-model="selectedDoc" />

            </div>
            <div class="table-control">
                Annotation:
                <GInput type="select" :options="annotationOptions" v-model="selectedAnnotation" />
            </div>
        </div>


        <div v-if="selectedDoc && selectedAnnotation" class="document">

            <template v-for="tc in termcomps.slice(firstRecord, firstRecord + rowsToDisplay)" :key="tc.refTerm.id">

                <span class="wordComparison" :class="{ 'incorrect': !annotationsEqual(tc) }">
                    {{ tc.refTerm.annotations["token"] }}
                    <SingleTermComparisonTable :hypoTerm="tc.hypoTerm" :refTerm="tc.refTerm" class="tooltip" />
                </span>

                <!-- add newline after . -->
                <br v-if="tc.refTerm.annotations['token'] == '.'" />
            </template>

            <Paginator v-if="termcomps.length > rowsToDisplay" v-model:first="firstRecord" :rows="rowsToDisplay"
                :totalRecords="termcomps.length"></Paginator>
        </div>
        <p v-else>Select a document and an annotation.</p>

    </GCard>
</template>

<script setup lang="ts">
// Library & API
import { onMounted, watch, computed, ref, Ref } from 'vue'
import * as API from '@/api/evaluation'
// Types & Stores
import { TermComparison, Term } from '@/types/evaluation'
import stores, { CorporaStore, JobsStore, ExportStore, DocumentsStore, JobSelectionStore } from "@/stores"
// Components
import { GInput, GCard } from "@/components"
import SingleTermComparisonTable from '@/components/tables/SingleTermComparisonTable.vue'
import Paginator from 'primevue/paginator';

// Stores
const documentsStore = stores.useDocuments() as DocumentsStore
const corporaStore = stores.useCorpora() as CorporaStore
const jobSelection = stores.useJobSelection() as JobSelectionStore

// Fields
const docNames = computed(() => documentsStore.available.map(doc => ({ value: doc.name, text: doc.name })))
const selectedDoc = ref(null)
const selectedAnnotation: Ref<string> = ref(null as any as string)
const annotationOptions = ref(null)
const termcomps = ref<TermComparison[]>(null)
const loading = ref(false)
/** Paginator */
const firstRecord = ref(0)
const rowsToDisplay = ref(200)

// Watches & mounts
watch(selectedDoc, async (newVal) => {
    if (newVal) {
        loading.value = true;
        API.getDocumentLayerComparison(corporaStore.activeUUID, jobSelection.hypothesisJobId, newVal, jobSelection.referenceJobId)
            .then(response => {
                termcomps.value = response.data
            }).finally(() => {
                loading.value = false;
            })
    }
})

watch(termcomps, () => {
    if (!termcomps.value) return
    annotationOptions.value = Object.keys(termcomps.value[0].refTerm.annotations).map(key => ({ value: key, text: key }))
})

// Methods
function annotationsEqual(tc: TermComparison) {
    const refAnnot = cleanAnnotation(tc.refTerm)
    const hypoAnnot = cleanAnnotation(tc.hypoTerm)
    return refAnnot == hypoAnnot
}

function cleanAnnotation(term: Term) {
    return term.annotations[selectedAnnotation.value]?.toLowerCase().replace("_", "")
}

</script>

<style lang="scss" scoped>
.document {
    padding: 2rem;
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