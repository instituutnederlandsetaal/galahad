<template>
    <GTable :columns :items :loading sortColumn="name" selectable v-model="selectedCorpus">
        <template #help>
            <slot name="help"></slot>
        </template>

        <template #header>
            <slot name="header"></slot>
        </template>

        <template #empty>
            <slot name="empty"></slot>
        </template>

        <!-- source cell -->
        <template #cell-source="data: TableData<CorpusMetadata>">
            <ExternalLink v-if="data.item.source?.url" :href="data.item.source?.url">
                {{ data.item.source?.name ?? data.item.source?.url }}
            </ExternalLink>
            <template v-else>{{ data.item.source?.name }}</template>
        </template>

        <!-- jobs cell -->
        <template #cell-jobs="data: TableData<CorpusMetadata>">
            <GSpinner small v-if="data.item.processing > 0" />
            {{ data.item.jobs }}
        </template>
    </GTable>
</template>

<script setup lang="ts">
import type { CorpusMetadata } from "@/types/corpora"
import { type Column, type TableData } from "@/types/ui/table"
import { formatBytes, formatDate, formatPeriod } from "@/ts/utils"
import useCorpora from "@/stores/corpora"

// --- props ---
const { filter } = defineProps<{ filter: (c: CorpusMetadata) => boolean }>()

// --- data ---
const { loading, corpusId, corpus, corpora } = storeToRefs(useCorpora())
const columns: Column<CorpusMetadata>[] = [
    { key: "name" },
    { key: "documents", label: "docs", align: "right" },
    { key: "size", align: "right", format: (c: CorpusMetadata): string => formatBytes(c.size) },
    {
        key: "period",
        align: "center",
        sortOn: (c: CorpusMetadata): string | undefined => formatPeriod(c.period),
        format: (c: CorpusMetadata): string | undefined => formatPeriod(c.period),
    },
    { key: "tagset" },
    { key: "source" },
    { key: "language" },
    { key: "modified", format: (c: CorpusMetadata): string => formatDate(c.modified) },
    { key: "shared", sortOn: sortShared, format: formatShared },
    { key: "jobs", label: "jobs", align: "right" },
]

// --- computed ---
const items = computed<CorpusMetadata[]>(() => {
    return corpora.value.filter((c: CorpusMetadata) => filter(c))
})
const selectedCorpus = computed<CorpusMetadata>({
    get: () => corpus.value,
    set: (value: CorpusMetadata) => {
        corpusId.value = value.uuid
    },
})

// --- methods ---
function formatShared(c: CorpusMetadata): string {
    if (c.dataset) return "Dataset"
    const numPeople = (c.collaborators?.length ?? 0) + (c.viewers?.length ?? 0)
    if (numPeople === 0) return "No one"
    return numPeople === 1 ? `${numPeople} person` : `${numPeople} people`
}

function sortShared(c: CorpusMetadata): number {
    if (c.dataset) return -1
    return c.collaborators.length + c.viewers.length
}
</script>

<style lang="scss" scoped>
.spinner {
    display: inline-block;
    padding-right: 0.5rem;
    padding-top: 0.25rem;
}
</style>
