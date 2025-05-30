<template>
    <GCard title="Entities View">
        <template #help>
            <p>
                Here you can see all the named entities in the selected hypothesis job.
            </p>
        </template>

        <GTable v-if="items" class="table" :title="`Entities in ${jobSelection.hypothesisJobId}`" :loading :items
            :columns compact>
            <template #table-empty-instruction>
                Select a hypothesis layer.
            </template>

            <template #cell="data: TableData<DocumentEntities>">
                <GButton v-if="data.item.document != 'total' && data.field.key != 'document'" class="button"
                    @click="selectedItem = data">
                    {{ data.value }}
                </GButton>
            </template>
        </GTable>

        <GModal :show="selectedItem" @hide="selectedItem = undefined"
            :title="`Entities in ${selectedItem?.item?.document}`">
            <template #help>
                Here you can view all the entities in the selected document.
            </template>
            <DocumentEntitiesTable :filter="selectedItem?.field?.key" :entities="selectedItem?.item?.entities">
            </DocumentEntitiesTable>
        </GModal>
    </GCard>
</template>

<script setup lang="ts">
import stores from '@/stores'
import * as API from '@/api/evaluation'
import type { Field, TableData } from '@/types/ui/table'
import { Entity, type DocumentEntities } from '@/types/evaluation/entities'

// --- stores ---
const jobSelection = stores.useJobSelection()
const corpora = stores.useCorpora()

// --- data ---
const loading = ref<boolean>(false)
const items = ref<DocumentEntities[]>()
const selectedItem = ref<TableData<DocumentEntities>>()

// --- computed ---
const columns = computed<Field[]>(() => Object.keys(items.value[0]).map((i) => ({ key: i })))

watch(() => jobSelection.hypothesisJobId, () => {
    if (!jobSelection.hypothesisJobId) return
    loading.value = true
    API.getJobEntities(
        corpora.activeUUID,
        jobSelection.hypothesisJobId,
    ).then(res => {
        items.value = Object.entries(res.data.documents).map(([key, value]) => ({ document: key, ...value.summary, total: value.total, entities: value.entities }))
        items.value.unshift({ document: "total", ...res.data.summary, total: res.data.total })
    }).finally(() => {
        loading.value = false
    })
}, { immediate: true })
</script>

<style scoped lang="scss">
.table {
    :deep(td) {
        padding: 0 !important;
        box-sizing: border-box !important;

        .button {
            width: 100%;
            height: 100%;
            background-color: transparent;

            &:hover {
                background-color: var(--int-light-grey) !important;
            }

            &:focus {
                background-color: var(--int-light-grey-hover) !important;
            }
        }
    }
}
</style>