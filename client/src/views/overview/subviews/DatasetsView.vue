<template>
    <GCard title="Datasets overview">
        <template #help>
            <BenchmarkSetsHelp />
        </template>

        <CorpusTable :type="TableCorporaType.Dataset" :corpora="corporaStore.datasetCorpora" selectable>
            
        </CorpusTable>
        <DocumentsTable :type="TableDocumentsType.Dataset" :corpus="corporaStore.activeCorpus">
            <template #help>
                Here you can see a small preview of the documents within the selected benchmark set.
            </template>
        </DocumentsTable>
        <GSpinner v-if="corporaStore.loading" class="spinner" />
    </GCard>
</template>

<script setup lang="ts">
// Libraries & stores

import stores from "@/stores"
// API & types
import {TableCorporaType, TableDocumentsType} from "@/types/table"

// Stores
const corporaStore = stores.useCorpora()

// Watches & mounts
// Only needs to load once.
onMounted(() => {
    corporaStore.reload()
})
</script>

<style scoped lang="scss">
.spinner {
    align-self: center;
}
</style>
