<template>
    <GTable :columns :items :loading sortColumn="name" selectable v-model="selectedCorpus">
        <template #help v-if="$slots.help">
            <slot name="help"></slot>
        </template>

        <template #table-empty>
            <slot name="table-empty">First, create a new corpus.</slot>
        </template>

        <!-- source cell -->
        <template #cell-source="data: TableData<CorpusMetadata>">
            <ExternalLink v-if="data.item.sourceUrl" :href="data.item.sourceUrl">
                {{ data.item.sourceName ? data.item.sourceName : data.item.sourceUrl }}
            </ExternalLink>
            <template v-else>{{ data.item.sourceName }}</template>
        </template>

        <!-- jobs cell -->
        <template #cell-activeJobs="data: TableData<CorpusMetadata>">
            {{ data.item.numResults }}
            <GSpinner small v-if="data.item.activeJobs > 0" />
        </template>
    </GTable>
</template>

<script setup lang="ts">
import stores from "@/stores"
import type { CorpusMetadata } from "@/types/corpora"
import { type Column, CorpusTableType, type TableData } from "@/types/ui/table"
import { formatBytes, formatDate } from "@/ts/utils"

// --- props ---
const { corpora, type } = defineProps<{ corpora: CorpusMetadata[]; type: CorpusTableType }>()

// --- stores ---
const userStore = stores.useUser()

// --- data ---
const { loading, corpusId, corpus } = storeToRefs(stores.useCorpora())
const selectedCorpus = computed<CorpusMetadata>({
    get: () => corpus.value,
    set: (value: CorpusMetadata) => {
        corpusId.value = value.uuid
    },
})
const columns: Column<CorpusMetadata>[] = [
    { key: "uuid", hidden: true },
    { key: "name" },
    { key: "numDocs", label: "docs", align: "right" },
    { key: "size", align: "right", format: (c: CorpusMetadata): string => formatBytes(c.size) },
    {
        key: "period",
        align: "center",
        sortOn: (c: CorpusMetadata): string => `${c.eraFrom} ${c.eraTo}`,
        format: (c: CorpusMetadata): string => `${c.eraFrom} – ${c.eraTo}`,
    },
    { key: "tagset" },
    { key: "source" },
    { key: "language" },
    { key: "modified", format: (c: CorpusMetadata): string => formatDate(c.modified) },
    {
        key: "collaborators",
        label: "shared with",
        align: "center",
        hidden: type === CorpusTableType.dataset,
        sortOn: (c: CorpusMetadata): number => sortShared(c),
        format: formatCollaborators,
    },
    { key: "activeJobs", label: "jobs", align: "center", hidden: type === CorpusTableType.dataset },
]

// --- computed ---
const items = computed(() => {
    if (type === CorpusTableType.user) {
        return corpora.filter((i) => i.owner === userStore.user.id)
    }
    return corpora
})

// --- methods ---
function formatCollaborators(i: CorpusMetadata): string {
    if (i.dataset) return "Dataset"
    const numPeople = i.collaborators.length + i.viewers.length
    if (numPeople === 0) return "No one"
    return numPeople === 1 ? `${numPeople} person` : `${numPeople} people`
}

function sortShared(i: CorpusMetadata): number {
    if (i.dataset) return -1
    return i.collaborators.length + i.viewers.length
}
</script>
