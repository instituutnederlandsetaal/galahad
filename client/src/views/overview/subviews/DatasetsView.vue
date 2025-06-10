<template>
    <GCard title="Datasets overview">
        <template #help>
            <BenchmarkSetsHelp />
        </template>

        <CorpusTable :type="TableCorporaType.dataset" :corpora="datasets" selectable />

        <DocumentsTable :type="TableDocumentsType.dataset" :corpus="dataset" :loading :documents="datasetDocs">
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
import { TableCorporaType, TableDocumentsType } from "@/types/ui/table"

const { datasets, corpus } = storeToRefs(stores.useCorpora())
const { documents, loading } = storeToRefs(stores.useDocuments())
const dataset = computed<CorpusMetadata | undefined>(
    (): CorpusMetadata | undefined =>
        corpus.value?.dataset ? corpus : undefined
)
const datasetDocs = computed<DocumentMetadata[]>((): DocumentMetadata[] =>
    corpus.value?.dataset ? documents.value : []
)
</script>
