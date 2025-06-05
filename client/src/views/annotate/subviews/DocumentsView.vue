<template>
    <AnnotateTab hideDocsError hideAnnotationsError>
        <GCard title="Documents" :helpLink="{ href: '/galahad/help/formats', subject: 'formats' }">
            <template #help>
                <slot name="help">
                    <DocumentsHelp />
                </slot>
            </template>
            <UploadDocuments v-if="userStore.canWrite" />
            <DocumentsTable :type="TableDocumentsType.User" :corpus="corpora.activeCorpus" />
        </GCard>
    </AnnotateTab>
</template>

<script setup lang="ts">
import stores from "@/stores"
import { TableDocumentsType } from "@/types/ui/table"

const corpora = stores.useCorpora()
const documents = stores.useDocuments()
const userStore = stores.useUser()

onMounted(() => {
    // Clear errors when this tab is opened.
    // Note that we can't put this inside DocumentsTable,
    // because on upload, AnnotateTab will mount it again with v-if.
    documents.clearUploadErrors()
})
</script>
