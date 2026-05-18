<template>
    <GTable :items="filteredItems" :columns sortColumn="count">
        <template #title>Entities</template>
        <template #help> Here you can view all the entities in the selected document. </template>
        <template #header>
            <form class="filter" @submit.prevent>
                <fieldset>
                    <label for="entities-select">Entities</label>
                    <MultiSelect
                        id="entities-select"
                        v-model="selectedEntities"
                        :options="entityOptions"
                        placeholder="Select entities"
                        :maxSelectedLabels="3"
                    />
                </fieldset>
                <fieldset>
                    <label for="jobs-select">Jobs</label>
                    <MultiSelect
                        id="jobs-select"
                        v-model="selectedJobs"
                        :options="jobOptions"
                        placeholder="Select jobs"
                        :maxSelectedLabels="3"
                    />
                </fieldset>
            </form>
        </template>
        <template #table-empty> No entities found. </template>
    </GTable>
</template>

<script setup lang="ts">
import type { Entity } from "@/types/evaluation/entities"
import type { Column } from "@/types/ui/table"
import MultiSelect from "primevue/multiselect"

const { entities } = defineProps<{ entities: Entity[]; filter?: string }>()

const jobOptions = computed<string[]>(() => Array.from(new Set(items.value.map((entity) => entity.job))))
const entityOptions = computed<string[]>(() => Array.from(new Set(items.value.map((entity) => entity.label))))
const selectedEntities = ref<string[]>([])
const selectedJobs = ref<string[]>([])

const filteredItems = computed(() => {
    return items.value.filter(filter)
})

const items = computed(() =>
    // entities is in the form: { job1: Entity[], job2: Entity[], ... }
    // We are going to expand it to a flat array of entities, with an extra property 'job'
    Object.entries(entities).flatMap(([jobName, jobEntities]) =>
        jobEntities.map((entity) => ({ ...entity, job: jobName })),
    ),
)
const columns = ref<Column<Entity>[]>([
    { key: "label", sortOn: (e: Entity): string => e.label },
    { key: "form", sortOn: (e: Entity): string => e.form },
    { key: "count", sortOn: (e: Entity): number => e.count },
    { key: "job", sortOn: (e: Entity): string => e.job },
])

function filter(entity: Entity): boolean {
    if (selectedEntities.value.length === 0 && selectedJobs.value.length === 0) return true
    // only one active
    if (selectedEntities.value.length === 0) return selectedJobs.value.includes(entity.job)
    if (selectedJobs.value.length === 0) return selectedEntities.value.includes(entity.label)

    return selectedEntities.value.includes(entity.label) && selectedJobs.value.includes(entity.job)
}
</script>

<style scoped lang="scss">
.filter {
    display: flex;
    flex-wrap: wrap;
    gap: 1rem;

    fieldset {
        display: flex;
        flex-direction: column;
        align-items: center;
    }
}
</style>
