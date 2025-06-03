<template>
    <GTable :items :columns sortColumn="count" compact>
        <template #table-empty-instruction>
            No entities found in this document.
        </template>
    </GTable>
</template>


<script setup lang="ts">
import type { Entity } from "@/types/evaluation/entities"
import type { Column } from "@/types/ui/table"

const { entities, filter } = defineProps<{
    entities: Entity[]
    filter?: string
}>()

const items = computed(() =>
    entities.filter(i => i.label === filter || filter === undefined),
)
const columns = ref<Column[]>([
    { key: "label", sortOn: i => i.label },
    { key: "form", sortOn: i => i.form },
    { key: "count", sortOn: i => i.count },
])
</script>
