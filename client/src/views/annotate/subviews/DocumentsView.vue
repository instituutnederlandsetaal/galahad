<template>
    <AnnotateTab hideDocsError hideAnnotationsError>
        <GCard title="Documents" :helpLink="{ href: '/galahad/help/formats', subject: 'formats' }">
            <template #help>
                <slot name="help">
                    <DocumentsHelp />
                </slot>
            </template>
            <UploadDocuments v-if="userStore.hasWriteAccess" />
            <DocumentsTable headless :type="TableDocumentsType.User" :corpus="corporaStore.activeCorpus" />
        </GCard>
    </AnnotateTab>
</template>

<script setup lang="ts">
// Libraries & stores
import stores from "@/stores"
// API & types
import { TableDocumentsType } from "@/types/ui/table"

// --- computed ---
const title = computed<string>(() => {
    if (!corporaStore.activeCorpus) {
        return "No documents"
    }
    const numDocs = documentsStore.available.length
    const documents = numDocs === 1 ? "document" : "documents"
    const corpusName = corporaStore.activeCorpus.name
    return `${numDocs} ${documents} in ${corpusName}`
})

// Stores
const corporaStore = stores.useCorpora()
const documentsStore = stores.useDocuments()
const userStore = stores.useUser()

// Watches & mounts
onMounted(() => {
    // Clear errors when this tab is opened.
    // Note that we can't put this inside DocumentsTable,
    // because on upload, AnnotateTab will mount it again with v-if.
    documentsStore.clearUploadErrors()
})
</script>
