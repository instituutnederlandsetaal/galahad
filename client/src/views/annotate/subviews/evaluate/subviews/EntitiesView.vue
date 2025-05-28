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

            <template #cell-second="data: TableData<Term>">
                {{ displayEntity(data.value) }}
            </template>
        </GTable>
    </GCard>
</template>

<script setup lang="ts">
import stores from '@/stores'
import type { Entity, Term } from '@/types/evaluation'
import type { Field, TableData } from '@/types/ui/table'
import * as API from '@/api/evaluation'

const jobSelection = stores.useJobSelection()
const corporaStore = stores.useCorpora()

const selectedDoc = ref<string>()

const items = ref<Entity[]>()
const title = computed<string>(() => {
    return `Entities in ${selectedDoc.value}`
})
const columns = ref<Field[]>([
    { key: "first", label: "NER" },
    { key: "second" },
    { key: "third", label: "Count" }
])

const loading = ref<boolean>(false)

watch(() => selectedDoc.value, () => {
    loading.value = true
    API.getEntities(
        corporaStore.activeUUID,
        jobSelection.hypothesisJobId,
        selectedDoc.value
    ).then(res => {
        items.value = res.data
    }).finally(() => {
        loading.value = false
    })
})

function displayEntity(terms: Term[]): string {
    return terms.map(term => term.annotations["token"]).join(' ')
}
</script>