<template>
    <GCard title="Entities View">
        <template #help>
            <p>
                Here you can see all the named entities in the selected document.
            </p>
        </template>
        <DocumentSelect v-if="jobSelection.hypothesisJobId" v-model="selectedDoc" />
        <p v-else>
            Please select a hypothesis job first.
        </p>
        <GTable compact v-if="items" :title :loading :items :columns>
            <template #table-empty-instruction>
                No entities found in this document.
            </template>
        </GTable>
    </GCard>
</template>

<script setup lang="ts">
import stores from '@/stores'
import * as API from '@/api/evaluation'
import type { Entity, Term } from '@/types/evaluation'
import type { Field, TableData } from '@/types/ui/table'

// --- stores ---
const jobSelection = stores.useJobSelection()
const corpora = stores.useCorpora()

// --- data ---
const selectedDoc = ref<string>()
const loading = ref<boolean>(false)
const items = ref<Entity[]>()
const columns = ref<Field[]>([
    { key: "first", label: "label", sortOn: (i) => i.first },
    { key: "second", label: "entity", sortOn: (i) => i.second },
    { key: "third", label: "count", sortOn: (i) => i.third }
])

// --- computed ---
const title = computed<string>(() => {
    return `Entities in ${selectedDoc.value}`
})


watch(() => selectedDoc.value, () => {
    loading.value = true
    API.getEntities(
        corpora.activeUUID,
        jobSelection.hypothesisJobId,
        selectedDoc.value
    ).then(res => {
        items.value = res.data
    }).finally(() => {
        loading.value = false
    })
})
</script>
