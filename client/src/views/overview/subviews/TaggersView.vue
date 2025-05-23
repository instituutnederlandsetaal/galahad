<template>
    <GCard title="Taggers overview">
        <template #help>
            <p>
                Here you can see an overview of all available taggers within GaLAHaD. <br />
                For more information on the taggers, please visit GitHub:
                <ExternalLink href="https://github.com/INL/galahad-taggers-dockerized/">
                    galahad-taggers-dockerized
                </ExternalLink>
            </p>
        </template>

        <GTable :loading="taggerStore.loading"  :columns :items="taggerStore.taggers" sortedByColumn="id">
            <template #table-empty-instruction>
                No taggers appeared? That is not right! Please contact the INT at
                <MailAddress />
            </template>

            <!-- id -->
            <template #cell-id="d">
                <span :class="markActive(d.item.id)">{{ d.value }}</span>
            </template>

            <!-- tagset -->
            <template #cell-tagset="d">
                <span v-if="d.value">{{ d.value }}</span>
                <i v-else>Unknown</i><br />
            </template>

            <!-- era -->
            <template #cell-era="d"> {{ d.item.eraFrom }} - {{ d.item.eraTo }} </template>

            <!-- annotations -->
            <template #cell-annotations="d">
                {{ sort_tagger_annotations(d.value).join(", ") }}
            </template>

            <!-- links -->
            <template #cell-model="d">
                <ExternalLink :href="d.value.href">
                    {{ d.value.name }}
                </ExternalLink>
            </template>
            <template #cell-software="d">
                <ExternalLink :href="d.value.href">
                    {{ d.value.name }}
                </ExternalLink>
            </template>
            <template #cell-dataset="d">
                <ExternalLink :href="d.value.href">
                    {{ d.value.name }}
                </ExternalLink>
            </template>
        </GTable>
    </GCard>
</template>

<script setup lang="ts">
// Libraries & stores
import stores from "@/stores"

// API & types
import { sort_tagger_annotations } from "@/stores/taggers"

// Stores
const taggerStore = stores.useTaggers()

// Fields
const columns = [
    { key: "id", label: "name", sortOn: (x: any) => x.id },
    { key: "description" },
    { key: "tagset", sortOn: (x: any) => x.tagset },
    {
        key: "era",
        label: "period",
        sortOn: (x: any) => x.eraFrom.toString() + x.eraTo.toString(),
    },
    { key: "annotations" },
    { key: "model" },
    { key: "software" },
    { key: "dataset" },
]

// Methods
/**
 * Mark the active row, retrieved from the url anchor.
 */
function markActive(id: string) {
    const hash = window.location.hash.substring(1)
    if (id === hash) {
        return "active"
    }
    return ""
}
</script>

<style scoped lang="scss">
:deep(tr):has(> td > span.active) {
    background-color: var(--int-theme-lighter);
}
</style>
