<template>
    <GTable :columns :items="displayCorpora" :loading="corporaStore.loading" sortColumn="name" selectable
        v-model="selectedCorpus">

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
        <template #cell-source="data">
            <ExternalLink v-if="data.item.sourceURL" :href="data.item.sourceURL">
                {{ data.item.sourceName ? data.item.sourceName : data.item.sourceURL }}
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
import { type Column, TableCorporaType, TableData } from "@/types/ui/table"
import { formatBytes, formatDate } from "@/ts/utils"

// Stores
const userStore = stores.useUser()
const corporaStore = stores.useCorpora()

// Props
const props = defineProps({
    corpora: Array<CorpusMetadata>,
    type: String as PropType<TableCorporaType>,
    selectable: Boolean
})

// Fields
const selectedCorpus = ref(corporaStore.activeCorpus)
const editable = props.type !== TableCorporaType.Dataset
const displayCorpora = computed(() => {
    if (props.type === TableCorporaType.User) {
        return props.corpora.filter(i => i.owner === userStore.user.id)
    }
    if (props.type === TableCorporaType.Shared) {
        return props.corpora.filter(
            i =>
                i.collaborators.includes(userStore.user.id) ||
                i.viewers.includes(userStore.user.id)
        )
    }
    return props.corpora
})
// Enable edit & delete buttons only if activeCorpus is in this table.
// (Not that CorpusForm cares, but looks nicer)
const activeCorpusInTable = computed(() => {
    return displayCorpora.value
        .map(i => i.uuid)
        .includes(selectedCorpus.value?.uuid)
})
const columns: Column[] = [
    { key: "uuid", isPrimaryField: true, hidden: true },
    { key: "name", sortOn: x => x.name },
    { key: "numDocs", sortOn: x => x.numDocs, label: "docs", align: "right" },
    {
        key: "sizeInBytes",
        label: "size",
        sortOn: x => x.sizeInBytes,
        align: "right",
        format: d => formatBytes(d.value)
    },
    {
        key: "period",
        sortOn: (x: any) => x.eraFrom.toString() + x.eraTo.toString(),
        align: "center"
    },
    { key: "tagset", sortOn: x => x.tagset },
    { key: "source", label: "source", sortOn: x => x.source },
    {
        key: "lastModified",
        sortOn: x => x.lastModified,
        label: "last modified",
        format: d => formatDate(d.value)
    },
    {
        key: "collaborators",
        hidden: !editable,
        sortOn: x => customSharedSort(x),
        label: "shared with",
        align: "center",
        format: d => formatCollaborators(d.item)
    },
    {
        key: "activeJobs",
        hidden: !editable,
        sortOn: x => x.activeJobs,
        label: "jobs",
        align: "center"
    }
]

// Watches
// We can't use corporaStore.activeCorpus directly, because there will be multiple corpus tables on the page.
watch(
    () => corporaStore.activeCorpus,
    () => {
        return (selectedCorpus.value = corporaStore.activeCorpus)
    },
    { immediate: true }
)
watch(
    () => selectedCorpus.value,
    () => {
        if (selectedCorpus.value) {
            corporaStore.activeUUID = selectedCorpus.value?.uuid
        }
    },
    { immediate: true }
)

// Methods
function formatCollaborators(i: CorpusMetadata): string {
    if (i.dataset) return "Dataset"
    const numPeople = i.collaborators.length + i.viewers.length
    if (numPeople === 0) return "No one"
    return numPeople === 1 ? `${numPeople} person` : `${numPeople} people`
}

function customSharedSort(i: CorpusMetadata) {
    if (i.dataset) return -1
    return i.collaborators.length + i.viewers.length
}
</script>
