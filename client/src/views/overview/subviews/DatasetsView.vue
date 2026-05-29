<template>
    <GCard>
        <CorporaTable title="Datasets" :filter="(c: CorpusMetadata) => c.dataset">
            <template #help>
                <BenchmarkSetsHelp />
            </template>
            <template #empty>No dataset corpora available.</template>
        </CorporaTable>

        <DocumentsTable :corpus="dataset" :loading :documents="datasetDocs" :layer="sourceLayer"> </DocumentsTable>
    </GCard>
</template>

<script setup lang="ts">
import useCorpora from "@/stores/corpora"
import useDocuments from "@/stores/documents"
import useLayers from "@/stores/layers"
import type { CorpusMetadata } from "@/types/corpora"
import type { DocumentMetadata } from "@/types/documents"

const { corpus, corpusId } = storeToRefs(useCorpora())
const { documents, loading } = storeToRefs(useDocuments())
const { sourceLayer, layers } = storeToRefs(useLayers())

const dataset = computed<CorpusMetadata | undefined>((): CorpusMetadata | undefined =>
    corpus.value?.dataset ? corpus.value : undefined,
)
const datasetDocs = computed<DocumentMetadata[]>((): DocumentMetadata[] => (dataset.value ? documents.value : []))

const { reload: reloadCorpora } = useCorpora()

onMounted(() => {
    corpus.value = undefined
    corpusId.value = undefined
    documents.value = []
    layers.value = []
    reloadCorpora()
})
</script>
