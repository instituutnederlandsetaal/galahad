<template>
    <GTable :columns :items="documentsStore.documents" :loading="documentsStore.loading" :displayOnEmpty="false"
        sortColumn="name" :sortDesc="false">
        <template #title>
            <span v-if="!corpus || (type == TableDocumentsType.Dataset && !corpus.dataset)">No documents</span>
            <span v-else>
                {{ documentsStore.documents.length }}
                {{ documentsStore.documents.length === 1 ? "document" : "documents" }}
                in corpus <i>{{ corpus.name }}</i>
            </span>
        </template>

        <template #help v-if="$slots.help">
            <slot name="help"></slot>
        </template>

        <template #table-empty>
            <span v-if="!corpus || (type == TableDocumentsType.Dataset && !corpus?.dataset)">No corpus selected.</span>
            <span v-else-if="corpus?.uuid && type != TableDocumentsType.Dataset">
                This corpus is empty. Upload documents to the corpus.
            </span>
        </template>

        <!-- name cell -->
        <template #cell-name="data">
            <div style="max-height: 3rem; line-break: anywhere; overflow: hidden; min-width: 80px">
                {{ data.value }}
            </div>
        </template>

        <!-- size cell -->
        <template #cell-size="data">
            {{ data.value }}
        </template>

        <!-- layerSummary cell -->
        <template #cell-layerSummary="data">
            <RightFloatCell>
                <template #left>
                    {{ data.value.tokens }}
                </template>
                <template #right>
                    <InspectButton v-if="data.value.tokens > 0" @click="previewDocument = data.item" />
                </template>
            </RightFloatCell>
        </template>

        <!-- plain text preview cell -->
        <template #cell-preview="data">
            <div style="min-width: 200px; max-height: 3rem; overflow: hidden">
                {{ data.value }}
            </div>
        </template>

        <!-- last modified cell -->
        <template #cell-lastModified="data">
            {{ formatDate(data.value) }}
        </template>

        <!-- actions cell -->
        <template #cell-actions="data">
            <div class="actions">
                <DownloadButton @click="download(data.item)" />

                <GButton red @click="
                    () => {
                        deleteDocumentData = data.item
                    }
                " title="Delete">
                    <i class="fa fa-trash"></i>
                </GButton>
            </div>
        </template>
    </GTable>

    <!-- preview modal -->
    <GModal v-if="previewDocument !== undefined" @hide="previewDocument = undefined">
        <LayerViewer :document="previewDocument" />
    </GModal>

    <!-- delete modal -->
    <DeleteModal v-if="deleteDocumentData" :itemName="`${deleteDocumentData.name} and associated results`"
        @hide="deleteDocumentData = undefined" @delete="deleteDocument(deleteDocumentData)" />
</template>

<script setup lang="ts">
// Libraries & stores

import stores from "@/stores"
// Utils
import { formatDate } from "@/ts/utils"
import type { CorpusMetadata } from "@/types/corpora"
import type { DocumentMetadata } from "@/types/documents"
// API & types
import { type Column, TableDocumentsType } from "@/types/ui/table"

// Stores
const documentsStore = stores.useDocuments()
const userStore = stores.useUser()

// Props
const props = defineProps({
    type: String as PropType<TableDocumentsType>, // the mode of the table
    corpus: { type: Object as PropType<CorpusMetadata>, default: null }
})

// Fields
const deleteDocumentData = ref(null as null | DocumentMetadata)
const previewDocument = ref<DocumentMetadata>()

const columns = computed<Column[]>(() => {
    const publicFields = [
        {
            key: "name",
            sortOn: (x: DocumentMetadata) => x.name
        },
        { key: "format", sortOn: (x: DocumentMetadata) => x.format },
        { key: "preview" },
        {
            key: "layerSummary",
            label: "tokens",
            align: "right",
            sortOn: (x: DocumentMetadata) => x.layerSummary?.tokens
        },
        {
            key: "lastModified",
            label: "last modified",
            sortOn: (x: DocumentMetadata) => x.lastModified
        }
    ] as Column[]
    if (userStore.canWrite && props.type === TableDocumentsType.User) {
        return publicFields.concat({ key: "actions" })
    }
    // public
    return publicFields
})

// Methods
function deleteDocument(document: DocumentMetadata) {
    return documentsStore.deleteDocument(document.name)
}
function download(document: DocumentMetadata) {
    return documentsStore.downloadRaw(document.name)
}
</script>

<style scoped lang="scss">
.actions {
    display: flex;
    gap: 0.25rem;
}
</style>
