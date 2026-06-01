<template>
    <GCard>
        <CorporaTable title="Datasets" :filter="(c: CorpusMetadata) => c.dataset">
            <template #help>
                <BenchmarkSetsHelp />
            </template>
            <template #empty>No dataset corpora available.</template>
        </CorporaTable>

        <DocumentsTable :layer="sourceLayer"> </DocumentsTable>
    </GCard>
</template>

<script setup lang="ts">
import useCorpora from "@/stores/corpora"
import useDocuments from "@/stores/documents"
import useLayers from "@/stores/layers"
import type { CorpusMetadata } from "@/types/corpora"

const { corpus, corpusId } = storeToRefs(useCorpora())
const { documents } = storeToRefs(useDocuments())
const { sourceLayer, layers } = storeToRefs(useLayers())

const { reload: reloadCorpora } = useCorpora()

onMounted(() => {
    corpus.value = undefined
    corpusId.value = undefined
    documents.value = []
    layers.value = []
    reloadCorpora()
})
</script>
