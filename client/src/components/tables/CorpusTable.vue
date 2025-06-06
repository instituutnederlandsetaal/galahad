<template>
    <GTable :columns :items="displayCorpora" :loading sortColumn="name" selectable v-model="selectedCorpus">

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

        <!-- era -->
        <template #cell-period="d">
            {{ d.item.eraFrom }} - {{ d.item.eraTo }}
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
import { type Column, TableCorporaType, type TableData } from "@/types/ui/table"
import { formatBytes, formatDate } from "@/ts/utils"

// Stores
const userStore = stores.useUser()
const { loading, activeUUID } = storeToRefs(stores.useCorpora())
const _selectedCorpus = ref<CorpusMetadata>()
const selectedCorpus = computed<CorpusMetadata>({
    get(): CorpusMetadata {
        return _selectedCorpus.value
    },
    set(newValue): void {
        activeUUID.value = newValue.uuid
        _selectedCorpus.value = newValue
    }
})

// Props
const { corpora, type } = defineProps<{
    corpora: CorpusMetadata[]
    type: TableCorporaType
}>()

// Fields
const displayCorpora = computed(() => {
    if (type === TableCorporaType.User) {
        return corpora.filter(i => i.owner === userStore.user.id)
    }
    if (type === TableCorporaType.Shared) {
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
        format: (d: TableData<CorpusMetadata>): string =>
            formatBytes(d.item.size)
    },
    {
        key: "period",
        align: "center",
        sortOn: (c: CorpusMetadata): string =>
            c.eraFrom.toString() + c.eraTo.toString()
    },
    { key: "tagset" },
    { key: "source" },
    {
        key: "modified",
        format: (d: TableData<CorpusMetadata>): string =>
            formatDate(d.item.modified)
    },
    {
        key: "collaborators",
        label: "shared with",
        align: "center",
        hidden: type === TableCorporaType.Dataset,
        sortOn: (c: CorpusMetadata): number => customSharedSort(c),
        format: (d: TableData<CorpusMetadata>): string =>
            formatCollaborators(d.item)
    },
    {
        key: "activeJobs",
        label: "jobs",
        align: "center",
        hidden: type === TableCorporaType.Dataset
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
