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
import useCorpora from "@/stores/corpora"
import useDocuments from "@/stores/documents"
import type { CorpusMetadata } from "@/types/corpora"
import type { DocumentMetadata } from "@/types/documents"
import { CorpusTableType, DocsTableType } from "@/types/ui/table"

const { datasets, corpus } = storeToRefs(useCorpora())
const { reload } = useCorpora()
const { documents, loading } = storeToRefs(useDocuments())

const dataset = computed<CorpusMetadata | undefined>((): CorpusMetadata | undefined =>
    corpus.value?.dataset ? corpus.value : undefined,
)
const datasetDocs = computed<DocumentMetadata[]>((): DocumentMetadata[] => (dataset.value ? documents.value : []))

onMounted(reload)
</script>
