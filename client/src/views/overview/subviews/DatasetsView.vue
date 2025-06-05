<template>
    <GCard title="Datasets overview">
        <template #help>
            <BenchmarkSetsHelp />
        </template>

        <CorpusTable :type="TableCorporaType.Dataset" :corpora="corporaStore.datasets" selectable />

        <DocumentsTable :type="TableDocumentsType.Dataset" :corpus="corporaStore.activeCorpus">
            <template #help>
                Here you can see a small preview of the documents within the selected benchmark set.
            </template>
        </DocumentsTable>
        <GSpinner v-if="corporaStore.loading" class="spinner" />
    </GCard>
</template>

<script setup lang="ts">
import stores from "@/stores"
import { TableCorporaType, TableDocumentsType } from "@/types/ui/table"

const corporaStore = stores.useCorpora()
</script>
