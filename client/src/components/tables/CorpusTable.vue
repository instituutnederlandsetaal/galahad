<template>
    <GTable :columns :items :loading sortColumn="name" selectable v-model="selectedCorpus">

        <template #title>
            <slot name="title">
                {{ type }} corpora
            </slot>
        </template>

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
import {
    type Column,
    type Item,
    TableCorporaType,
    type TableData
} from "@/types/ui/table"
import { formatBytes, formatDate } from "@/ts/utils"

// Stores
const userStore = stores.useUser()
const { corpus } = storeToRefs(stores.useCorpora())
const { loading, corpusId } = storeToRefs(stores.useCorpora())
const _selectedCorpus = ref<CorpusMetadata>(corpus.value)
const selectedCorpus = computed<CorpusMetadata>({
    get(): CorpusMetadata {
        return _selectedCorpus.value
    },
    set(newValue): void {
        corpusId.value = newValue.uuid
        _selectedCorpus.value = newValue
    }
})

// Props
const { corpora, type } = defineProps<{
    corpora: CorpusMetadata[]
    type: TableCorporaType
}>()

// Fields
const items = computed(() => {
    if (type === TableCorporaType.user) {
        return corpora.filter(i => i.owner === userStore.user.id)
    }
    if (type === TableCorporaType.shared) {
        return corpora.filter(
            i =>
                i.collaborators.includes(userStore.user.id) ||
                i.viewers.includes(userStore.user.id)
        )
    }
    return corpora
})
const columns: Column<CorpusMetadata>[] = [
    { key: "uuid", hidden: true },
    { key: "name" },
    { key: "numDocs", label: "docs", align: "right" },
    {
        key: "size",
        align: "right",
        format: (c: CorpusMetadata): string => formatBytes(c.size)
    },
    {
        key: "period",
        align: "center",
        sortOn: (c: CorpusMetadata): string => `${c.eraFrom} ${c.eraTo}`,
        format: (c: CorpusMetadata): string => `${c.eraFrom} – ${c.eraTo}`
    },
    { key: "tagset" },
    { key: "source" },
    {
        key: "modified",
        format: (c: CorpusMetadata): string => formatDate(c.modified)
    },
    {
        key: "collaborators",
        label: "shared with",
        align: "center",
        hidden: type === TableCorporaType.dataset,
        sortOn: (c: CorpusMetadata): number => customSharedSort(c),
        format: formatCollaborators
    },
    {
        key: "activeJobs",
        label: "jobs",
        align: "center",
        hidden: type === TableCorporaType.dataset
    }
]

// Methods
function formatCollaborators(i: CorpusMetadata): string {
    if (i.dataset) return "Dataset"
    const numPeople = i.collaborators.length + i.viewers.length
    if (numPeople === 0) return "No one"
    return numPeople === 1 ? `${numPeople} person` : `${numPeople} people`
}

function customSharedSort(i: CorpusMetadata): number {
    if (i.dataset) return -1
    return i.collaborators.length + i.viewers.length
}
</script>
