<template>
    <AnnotateTab hideDocsError hideAnnotationsError>
        <GCard title="Documents" :helpLink="{ href: '/galahad/help/formats', subject: 'formats' }">
            <template #help>
                <slot name="help">
                    <DocumentsHelp />
                </slot>
            </template>
            <UploadDocuments v-if="canWrite" />
            <DocumentsTable :corpus :loading :documents :type="DocsTableType.user" />
        </GCard>
    </AnnotateTab>
</template>

<script setup lang="ts">
import stores from "@/stores"
import { DocsTableType } from "@/types/ui/table"

const { corpus } = storeToRefs(stores.useCorpora())
const { canWrite } = storeToRefs(stores.useUser())
const { documents, loading } = storeToRefs(stores.useDocuments())
const { reload } = stores.useDocuments()

// #lifecycle
onMounted(reload)
</script>
