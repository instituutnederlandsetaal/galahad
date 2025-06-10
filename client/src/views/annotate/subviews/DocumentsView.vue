<template>
    <AnnotateTab hideDocsError hideAnnotationsError>
        <GCard title="Documents" :helpLink="{ href: '/galahad/help/formats', subject: 'formats' }">
            <template #help>
                <slot name="help">
                    <DocumentsHelp />
                </slot>
            </template>
            <UploadDocuments v-if="userStore.canWrite" />
            <DocumentsTable :type="TableDocumentsType.user" :corpus :loading :documents />
        </GCard>
    </AnnotateTab>
</template>

<script setup lang="ts">
import stores from "@/stores"
import { TableDocumentsType } from "@/types/ui/table"

const { corpus } = storeToRefs(stores.useCorpora())
const userStore = stores.useUser()
const { documents, loading } = storeToRefs(stores.useDocuments())
const { clearUploadErrors } = stores.useDocuments()

onMounted(() => {
    // Clear errors when this tab is opened.
    // Note that we can't put this inside DocumentsTable,
    // because on upload, AnnotateTab will mount it again with v-if.
    clearUploadErrors()
})
</script>
