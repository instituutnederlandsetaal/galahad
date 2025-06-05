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

        <GTable :loading="taggers.loading" :columns :items="taggers.taggers" sortColumn="id">
            <template #table-empty>
                No taggers appeared? That is not right! Please contact the INT at
                <MailAddress />
            </template>

            <!-- id -->
            <template #cell-id="d">
                <span :id="d.value" :class="markActive(d.value)">{{ d.value }}</span>
            </template>

            <!-- era -->
            <template #cell-era="d">
                {{ d.item.eraFrom }} - {{ d.item.eraTo }}
            </template>

            <!-- annotations -->
            <template #cell-annotations="d">
                {{ d.value.join(", ") }}
            </template>

            <!-- links -->
            <template v-for="cell in ['cell-model', 'cell-software', 'cell-dataset']" #[cell]="d">
                <div :key="cell">
                    <ExternalLink :href="d.value.href">
                        {{ d.value.name }}
                    </ExternalLink>
                </div>
            </template>
        </GTable>
    </GCard>
</template>

<script setup lang="ts">
import stores from "@/stores"

const taggers = stores.useTaggers()

const columns = [
    { key: "id", label: "name", sortOn: (x: any) => x.id },
    { key: "description" },
    { key: "tagset", sortOn: (x: any) => x.tagset },
    {
        key: "era",
        label: "period",
        sortOn: (x: any) => x.eraFrom.toString() + x.eraTo.toString()
    },
    { key: "annotations" },
    { key: "model" },
    { key: "software" },
    { key: "dataset" }
]

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
