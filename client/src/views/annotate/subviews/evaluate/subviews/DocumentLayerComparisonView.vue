<template>
    <div>
        <p v-if="loading">Loading...</p>
        <GInput type="select" :options="docNames" v-model="selectedDoc" />
        <GInput type="select" :options="annotationOptions" v-model="selectedAnnotation" />
        <div v-if="selectedDoc && selectedAnnotation">
            <table v-for="tc in termcomps" :key="tc.refTerm.targets[0].id" class="wordComparison"
                :class="{ 'incorrect': tc.refTerm.annotations[selectedAnnotation] != tc.hypoTerm.annotations[selectedAnnotation] }">
                <tbody>
                    <tr>
                        {{ tc.refTerm.targets[0].literal }}
                    </tr>
                    <tr>
                        {{ tc.refTerm.annotations[selectedAnnotation] }}
                    </tr>
                    <tr>
                        {{ tc.hypoTerm.annotations[selectedAnnotation] }}
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<script setup lang="ts">
// Library & API
import { onMounted, watch, computed, ref } from 'vue'
import * as API from '@/api/evaluation'
// Types & Stores
import { TermComparison } from '@/types/evaluation'
import stores, { CorporaStore, JobsStore, ExportStore, DocumentsStore } from "@/stores"
// Components
import { GInput } from "@/components"

// Stores
const documentsStore = stores.useDocuments() as DocumentsStore
const corporaStore = stores.useCorpora() as CorporaStore
const jobSelection = stores.useJobSelection() as JobSelectionStore

// Fields
const docNames = computed(() => documentsStore.available.map(doc => ({ value: doc.name, text: doc.name })))
const selectedDoc = ref(null)
const selectedAnnotation = ref(null)
const annotationOptions = ref(null)
const termcomps = ref<TermComparison[]>(null)
const loading = ref(false)

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
    annotationOptions.value = Object.keys(termcomps.value[0].hypoTerm.annotations).map(key => ({ value: key, text: key }))
})
</script>

<style lang="scss" scoped>
.wordComparison {
    display: inline-block;
    margin: 0;
    margin-right: 0.1rem;
    border: 1px solid var(--int-light-grey);
    padding: 0.1rem;
}

.incorrect {
    border-color: red;
}
</style>