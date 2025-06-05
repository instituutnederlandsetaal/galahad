<template>
    <GTable :items :columns sortColumn="count" compact>
        <template #table-empty>
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
    // entities is in the form: { job1: Entity[], job2: Entity[], ... }
    // We are going to expand it to a flat array of entities, with an extra property 'job'
    Object.entries(entities).flatMap(([jobName, jobEntities]) =>
        jobEntities.map(entity => ({ ...entity, job: jobName }))
    )
)
const columns = ref<Column[]>([
    { key: "label", sortOn: i => i.label },
    { key: "form", sortOn: i => i.form },
    { key: "count", sortOn: i => i.count },
    { key: "job", sortOn: i => i.job }
])
</script>
