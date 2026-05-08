<template>
    <GCard title="Datasets overview">
        <template #help>
            <BenchmarkSetsHelp />
        </template>

        <CorpusTable :type="CorpusTableType.dataset" :corpora="datasets" selectable title="Benchmark corpora">
            <template #table-empty>No benchmark corpora available.</template>
        </CorpusTable>

        <DocumentsTable :type="DocsTableType.dataset" :corpus="dataset" :loading :documents="datasetDocs">
            <template #help>
                Here you can see a small preview of the documents within the selected benchmark set.
            </template>
        </DocumentsTable>
    </GCard>
</template>

<script setup lang="ts">
import stores from "@/stores"
import type { CorpusMetadata } from "@/types/corpora"
import type { DocumentMetadata } from "@/types/documents"
import { CorpusTableType, DocsTableType } from "@/types/ui/table"

// #stores
const { datasets, corpus, corpusId } = storeToRefs(stores.useCorpora())
const { documents, loading } = storeToRefs(stores.useDocuments())
const { reload } = stores.useDocuments()

// #computed
const dataset = computed<CorpusMetadata | undefined>((): CorpusMetadata | undefined =>
    corpus.value?.dataset ? corpus.value : undefined,
)
const datasetDocs = computed<DocumentMetadata[]>((): DocumentMetadata[] => (dataset.value ? documents.value : []))

// #watch
watch(corpusId, reload)
</script>
