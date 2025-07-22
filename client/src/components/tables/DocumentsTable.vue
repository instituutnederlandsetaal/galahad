<template>
    <GTable :columns :items="documents" :loading sortColumn="name">
        <template #title>
            <span v-if="!corpus">No documents</span>
            <span v-else>
                {{ documents.length }}
                {{ documents.length === 1 ? "document" : "documents" }}
                in corpus <i>{{ corpus.name }}</i>
            </span>
        </template>

        <template #help v-if="$slots.help">
            <slot name="help"></slot>
        </template>

        <template #table-empty>
            <template v-if="!corpus"> No corpus selected. </template>
            <template v-else> This corpus is empty. </template>
        </template>

        <template #cell-summary="data: TableData<DocumentMetadata>">
            <RightFloatCell>
                <template #left>
                    {{ data.value.annotations.token }}
                </template>
                <template #right>
                    <InspectButton v-if="data.value.annotations.token > 0" @click="previewDocument = data.item" />
                </template>
            </RightFloatCell>
        </template>

        <template #cell-actions="data: TableData<DocumentMetadata>">
            <GForm gap=".25rem">
                <DownloadButton @click="downloadRaw(data.item.name)" />

                <GButton red title="Delete" @click="deleteDocumentData = data.item">
                    <i class="fa fa-trash"></i>
                </GButton>
            </GForm>
        </template>
    </GTable>

    <GModal v-if="previewDocument !== undefined" @hide="previewDocument = undefined">
        <LayerViewer :document="previewDocument" />
    </GModal>

    <DeleteModal
        v-if="deleteDocumentData"
        :itemName="`${deleteDocumentData.name} and associated results`"
        @hide="deleteDocumentData = undefined"
        @delete="deleteDocument(deleteDocumentData.name)"
    />
</template>

<script setup lang="ts">
import stores from "@/stores"
import { formatDate } from "@/ts/utils"
import type { CorpusMetadata } from "@/types/corpora"
import type { DocumentMetadata } from "@/types/documents"
import { type Column, type TableData, DocsTableType } from "@/types/ui/table"

// Stores
const { deleteDocument, downloadRaw } = stores.useDocuments()
const { canWrite } = storeToRefs(stores.useUser())

// --- props ---
const { type, corpus, documents, loading } = defineProps<{
    type: DocsTableType
    corpus?: CorpusMetadata
    documents: DocumentMetadata[]
    loading: boolean
}>()

// --- data ---
const deleteDocumentData = ref<DocumentMetadata>()
const previewDocument = ref<DocumentMetadata>()

// --- computed ---
const columns = computed<Column<DocumentMetadata>[]>(() => [
    { key: "name" },
    { key: "format" },
    { key: "text" },
    {
        key: "summary",
        label: "tokens",
        align: "right",
        sortOn: (d: DocumentMetadata): number => d.summary?.annotations.token,
    },
    { key: "modified", format: (d: DocumentMetadata): string => formatDate(d.modified) },
    { key: "actions", noSort: true, hidden: !canWrite || type === DocsTableType.dataset },
])
</script>

