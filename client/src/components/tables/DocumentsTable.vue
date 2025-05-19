<template>
    <div>
        <GTable
            :columns
            :items="documentsStore.available"
            :loading="documentsStore.loading"
            :displayOnEmpty="false"
            sortedByColumn="name"
            :sortDesc="false"
            hoverRow
        >
            <template #title>
                <span v-if="!corpus || (type == TableDocumentsType.Dataset && !corpus.dataset)"> No documents </span>
                <span v-else>
                    {{ documentsStore.available.length }}
                    {{ documentsStore.available.length === 1 ? "document" : "documents" }}
                    in corpus <i>{{ corpus.name }}</i>
                </span>
            </template>

            <template #help>
                <slot name="help">
                    <component :is="help.documents"></component>
                </slot>
            </template>

            <template #table-empty-instruction>
                <span v-if="!corpus || (type == TableDocumentsType.Dataset && !corpus?.dataset)"
                    >No corpus selected.</span
                >
                <span v-else-if="corpus?.uuid && type != TableDocumentsType.Dataset" style="margin-top: 10px">
                    This corpus is empty. Upload documents to the corpus.
                </span>
            </template>

            <template #header>
                <UploadDocuments
                    v-if="userStore.hasWriteAccess && type != TableDocumentsType.Dataset"
                    style="margin-bottom: 1em"
                />
            </template>

            <!-- name cell -->
            <template #cell-name="data">
                <div style="max-height: 3em; line-break: anywhere; overflow: hidden; min-width: 80px">
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
                        <InspectButton
                            v-if="data.value.tokens > 0"
                            @click="
                                () => {
                                    preview = data.value.layerPreview
                                    previewDocument = data.item
                                }
                            "
                        />
                    </template>
                </RightFloatCell>
            </template>

            <!-- plain text preview cell -->
            <template #cell-preview="data">
                <div style="min-width: 200px; max-height: 3em; overflow: hidden">
                    {{ data.value }}
                </div>
            </template>

            <!-- last modified cell -->
            <template #cell-lastModified="data">
                <span style="white-space: nowrap">{{ formatDate(data.value) }}</span>
            </template>

            <!-- actions cell -->
            <template #cell-actions="data">
                <div style="display: flex">
                    <DownloadButton @click="download(data.item)" />

                    <GButton
                        red
                        @click="
                            () => {
                                deleteDocumentData = data.item
                                showDeleteModal = true
                            }
                        "
                        title="Delete"
                    >
                        <i class="fa fa-trash"></i>
                    </GButton>
                </div>
            </template>
        </GTable>

        <!-- preview modal -->
        <GModal
            :show="previewDocument != null"
            @hide="previewDocument = null"
            :title="`Preview of document ${previewDocument?.name}`"
            style="text-align: center"
        >
            <template #title>Source layer preview of document {{ previewDocument?.name }}</template>
            <template #help> Here you can inspect a small part of the source layer of the document. </template>
            <LayerViewer :layer="previewDocument?.layerPreview" />
        </GModal>

        <!-- delete modal -->
        <DeleteModal
            :show="showDeleteModal"
            :item="deleteDocumentData"
            :displayname="
                'document ' +
                (deleteDocumentData !== null ? deleteDocumentData.name : '[null]') +
                ' and associated results'
            "
            @hide="showDeleteModal = false"
            @delete="deleteDocument"
        />
    </div>
</template>

<script setup lang="ts">
// Libraries & stores
import { computed, ref, PropType, watch, onMounted } from "vue"
import stores from "@/stores"
// API & types
import { TableDocumentsType, Field } from "@/types/table"
import { DocumentMetadata } from "@/types/documents"
import { CorpusMetadata } from "@/types/corpora"
import { LayerPreview } from "@/types/jobs"
// Utils
import { formatDate } from "@/types/date"
// Components
import { GButton, GModal, GTable, DownloadButton, DeleteModal, RightFloatCell, InspectButton } from "@/components"
import LayerViewer from "@/components/tables/LayerViewer.vue"
import UploadDocuments from "@/components/input/UploadDocuments.vue"
import help from "@/components/help"

// Stores
const documentsStore = stores.useDocuments()
const userStore = stores.useUser()

// Props
const props = defineProps({
    type: String as PropType<TableDocumentsType>, // the mode of the table
    corpus: { type: Object as PropType<CorpusMetadata>, default: null },
})

// Fields
const deleteDocumentData = ref(null as null | DocumentMetadata)
const previewDocument = ref(null as null | DocumentMetadata)
const preview = ref(null as null | LayerPreview)
const showDeleteModal = ref(false)

const columns = computed<Field[]>(() => {
    const publicFields = [
        { key: "name", sortOn: (x: DocumentMetadata) => x.name, textAlign: "left" },
        { key: "format", sortOn: (x: DocumentMetadata) => x.format },
        { key: "preview", textAlign: "left" },
        {
            key: "layerSummary",
            label: "tokens",
            sortOn: (x: DocumentMetadata) => x.layerSummary?.tokens,
        },
        {
            key: "lastModified",
            label: "last modified",
            sortOn: (x: DocumentMetadata) => x.lastModified,
        },
    ] as Field[]
    if (userStore.hasWriteAccess && props.type == TableDocumentsType.User) {
        return publicFields.concat({ key: "actions" })
    } else {
        // public
        return publicFields
    }
})

// Methods
function deleteDocument(document: DocumentMetadata) {
    return documentsStore.deleteDocument(document.name)
}
function download(document: DocumentMetadata) {
    return documentsStore.downloadRaw(document.name)
}

// Watches & mounts
// Reload docs on uuid change (and onMounted). But don't show user docs on dataset tab.
watch(
    () => props.corpus?.uuid,
    () => {
        if (props.type == TableDocumentsType.Dataset && !props.corpus?.dataset) return
        documentsStore.reloadDocumentsForCorpus(props.corpus?.uuid)
    },
    { immediate: true },
)
// Reset any previous selection.
// E.g. when switching between datasets and user corpora.
onMounted(() => {
    documentsStore.available = []
})
</script>
