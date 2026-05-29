<template>
    <GTable title="Documents" :columns :items :loading sortColumn="name">
        <template #help>
            <slot name="help">
                Here you can see the documents in the selected
                {{ layer?.tagger?.name == SOURCE_LAYER ? "corpus" : "job" }} and a preview and summary of their
                annotations.
            </slot>
        </template>

        <template #empty>
            <template v-if="!corpus"> No corpus selected. </template>
            <template v-else> This corpus is empty. </template>
        </template>

        <template #header>
            <slot name="header"></slot>
            <aside v-if="layer?.documents" style="text-align: center">
                <p>Summary of {{ items.length }} {{ items.length === 1 ? "document" : "documents" }}:</p>
                <AnnotationSummary :annotations="layer?.annotations" />
            </aside>
        </template>

        <template #cell-annotations="data: TableData<DocumentMetadata>">
            <RightFloatCell>
                <template #left> {{ data.item.annotations.token }} </template>
                <template #right>
                    <InspectButton v-if="data.item.annotations.token > 0" @click="previewDocument = data.item" />
                </template>
            </RightFloatCell>
        </template>

        <template #cell-actions="data: TableData<DocumentMetadata>">
            <GForm gap=".25rem">
                <DownloadButton @click="download(data.item.name)" />

                <DeleteButton @click="deleteDocument = data.item" />
            </GForm>
        </template>
    </GTable>

    <GModal v-if="previewDocument !== undefined" @hide="previewDocument = undefined">
        <LayerViewer :document="previewDocument" />
    </GModal>

    <DeleteModal
        v-if="deleteDocument"
        :itemName="`${deleteDocument.name} and associated results`"
        @hide="deleteDocument = undefined"
        @delete="remove(deleteDocument.name)"
    />
</template>

<script setup lang="ts">
import useCorpora from "@/stores/corpora"
import useDocuments from "@/stores/documents"
import { formatDate } from "@/ts/utils"
import type { DocumentMetadata } from "@/types/documents"
import { type Column, type TableData } from "@/types/ui/table"
import type { LayerMetadata } from "@/types/layers"
import { SOURCE_LAYER } from "@/types/jobs"

// Stores
const { remove, download } = useDocuments()
const { canWrite, corpus } = storeToRefs(useCorpora())
const { loading, documents: items } = storeToRefs(useDocuments())

// --- props ---
const { actionable = false, layer } = defineProps<{ actionable?: boolean; layer?: LayerMetadata }>()

// --- data ---
const deleteDocument = ref<DocumentMetadata>()
const previewDocument = ref<DocumentMetadata>()

// --- computed ---
const columns = computed<Column<DocumentMetadata>[]>(() => [
    { key: "name" },
    { key: "format" },
    { key: "text" },
    {
        key: "annotations",
        label: "tokens",
        align: "right",
        sortOn: (d: DocumentMetadata): number => d.annotations.token,
    },
    { key: "modified", format: (d: DocumentMetadata): string => formatDate(d.modified) },
    { key: "actions", noSort: true, hidden: !canWrite.value || !actionable },
])
</script>
