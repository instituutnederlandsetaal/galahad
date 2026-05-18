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
            <ExternalLink v-if="data.item.source?.url" :href="data.item.source?.url">
                {{ data.item.source?.name ? data.item.source?.name : data.item.source?.url }}
            </ExternalLink>
            <template v-else>{{ data.item.source?.name }}</template>
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
        sortOn: (c: CorpusMetadata): string => formatPeriod(c),
        format: (c: CorpusMetadata): string => formatPeriod(c),
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
        return corpora.filter((i) => i.owner === userStore.user.name)
    }
    return corpora
})

// --- methods ---
function formatCollaborators(i: CorpusMetadata): string {
    if (i.dataset) return "Dataset"
    const numPeople = (i.collaborators?.length ?? 0) + (i.viewers?.length ?? 0)
    if (numPeople === 0) return "No one"
    return numPeople === 1 ? `${numPeople} person` : `${numPeople} people`
}

function sortShared(i: CorpusMetadata): number {
    if (i.dataset) return -1
    return i.collaborators.length + i.viewers.length
}

function formatPeriod(meta: CorpusMetadata): string | undefined {
    if (meta.period) {
        const from = meta.period.from ?? 0
        const to = meta.period.to ?? 0
        return `${from} – ${to}`
    } else {
        return undefined
    }
}
</script>
