<template>
    <div>
        <p v-if="loading">Loading...</p>
        <GInput type="select" :options="docNames" v-model="selectedDoc" />
        <GInput type="select" :options="annotationOptions" v-model="selectedAnnotation" />
        <div v-if="selectedDoc && selectedAnnotation" class="document">

            <template v-for="tc in termcomps" :key="tc.refTerm.targets[0].id">

                <span class="wordComparison" :class="{ 'incorrect': !annotationsEqual(tc) }">
                    {{ tc.refTerm.targets[0].literal }}
                    <table class="tooltip">
                        <tbody>
                            <tr>
                                <td>
                                    <b>{{ jobSelection.referenceJobId }}</b>
                                </td>
                                <td>
                                    {{ tc.refTerm.annotations[selectedAnnotation] ?? 'MISSING' }}
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <b>{{ jobSelection.hypothesisJobId }}</b>
                                </td>
                                <td>
                                    {{ tc.hypoTerm.annotations[selectedAnnotation] ?? 'MISSING' }}
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </span>

                <!-- add newline after . -->
                <br v-if="tc.refTerm.targets[0].literal == '.'" />

            </template>

            <!-- <table v-for="tc in termcomps" :key="tc.refTerm.targets[0].id" class="wordComparison"
                :class="{ 'incorrect': tc.refTerm.annotations[selectedAnnotation] != tc.hypoTerm.annotations[selectedAnnotation] }">
                <tbody>
                    <tr>
                        <b>{{ tc.refTerm.targets[0].literal }}</b>
                    </tr>
                    <tr>
                        {{ tc.refTerm.annotations[selectedAnnotation] ?? 'MISSING' }}
                    </tr>
                    <tr>
                        {{ tc.hypoTerm.annotations[selectedAnnotation] ?? 'MISSING' }}
                    </tr>
                </tbody>
            </table> -->
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
    annotationOptions.value = Object.keys(termcomps.value[0].refTerm.annotations).map(key => ({ value: key, text: key }))
})

// Methods
function annotationsEqual(tc: TermComparison) {
    return tc.refTerm.annotations[selectedAnnotation.value]?.toLowerCase().replace("_", "") == tc.hypoTerm.annotations[selectedAnnotation.value]?.toLowerCase()
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
    background-color: var(--white);
    border: 1px solid var(--int-grey);
    width: max-content;
    bottom: 100%;
    padding: 0.3rem;
    text-align: left;
}

.wordComparison:hover .tooltip {
    visibility: visible;
}
</style>