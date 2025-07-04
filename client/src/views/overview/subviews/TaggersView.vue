<template>
    <GCard title="Taggers overview">
        <template #help>
            <p>Here you can see an overview of all available taggers within GaLAHaD.</p>
            <p>
                For more information on the taggers, please visit
                <ExternalLink href="https://github.com/INL/galahad-taggers-dockerized/">
                    galahad-taggers-dockerized on GitHub </ExternalLink
                >.
            </p>
        </template>

        <GTable :loading :columns :items sortColumn="id">
            <template #table-empty>
                No taggers appeared? That is not right! Please contact the INT at
                <MailAddress />.
            </template>

            <!-- id -->
            <template #cell-id="d: TableData<Tagger>">
                <span :id="d.item.id" :class="markActive(d.item.id)">{{ d.value }}</span>
            </template>

            <!-- links -->
            <template v-for="cell in ['cell-model', 'cell-software', 'cell-dataset']" :key="cell" #[cell]="d">
                <ExternalLink :href="d.value.href">
                    {{ d.value.name }}
                </ExternalLink>
            </template>
        </GTable>
    </GCard>
</template>

<script setup lang="ts">
import stores from "@/stores"
import type { Tagger } from "@/types/taggers"
import type { Column, TableData } from "@/types/ui/table"

// #stores
const { taggers: items, loading } = storeToRefs(stores.useTaggers())

// #data
const columns: Column<Tagger>[] = [
    { key: "id", label: "name" },
    { key: "description" },
    { key: "tagset" },
    {
        key: "era",
        label: "period",
        sortOn: (t: Tagger): string => `${t.eraFrom} ${t.eraTo}`,
        format: (t: Tagger): string => `${t.eraFrom} – ${t.eraTo}`,
    },
    {
        key: "annotations",
        format: (t: Tagger): string => t.annotations.join(", "),
        sortOn: (t: Tagger): string => t.annotations.join(),
    },
    { key: "model" },
    { key: "software" },
    { key: "dataset" },
]

// #methods
/**
 * Mark the active row, retrieved from the url anchor.
 */
function markActive(id: string): string {
    const hash = window.location.hash.substring(1)
    return id === hash ? "active" : ""
}
</script>

<style scoped lang="scss">
:deep(tr):has(> td > span.active) {
    background-color: var(--int-theme-lighter);
}
</style>
