<!-- AnnotateTab takes care of error messages for unselected and empty corpora for the subtabs under annotate/* -->
<template>
    <!-- This div fills the background color. -->
    <!-- loading corpora-->
    <GCard v-if="corporaStore.loading && !hideCorpusError" title="Loading corpora">
        <GSpinner />
    </GCard>
    <!-- No corpus selected -->
    <GCard v-else-if="!corporaStore.activeCorpus && !hideCorpusError" title="No corpus selected">
        <GInfo error>
            <p>No corpus has been selected.</p>
            <GNav :route="{ path: '/annotate/corpora' }">Select a corpus</GNav>
        </GInfo>
    </GCard>
    <!-- No write permissions on selected corpus -->
    <GCard v-else-if="!userStore.canWrite && !hidePermissionsError" title="Insufficient permissions">
        <GInfo error>
            <p>You have insufficient permissions to perform this action.</p>
            <GNav :route="{ path: '/annotate/corpora' }">Select a different corpus</GNav>
        </GInfo>
    </GCard>
    <!-- Loading documents -->
    <GCard v-else-if="documentsStore.loading && !hideDocsError" title="Loading documents">
        <GSpinner />
    </GCard>
    <!-- No documents in corpus-->
    <GCard v-else-if="!corporaStore.hasDocs && !hideDocsError" title="Empty corpus">
        <GInfo error>
            <p>This corpus has no documents.</p>
            <GNav :route="{ path: '/annotate/documents' }">Upload documents to this corpus</GNav>
        </GInfo>
    </GCard>
    <!-- Loading jobs -->
    <GCard v-else-if="jobsStore.loading && !hideAnnotationsError" title="Loading jobs">
        <GSpinner />
    </GCard>
    <!-- No non-empty jobs-->
    <GCard v-else-if="jobSelectionStore.selectableJobs.length == 0 && !hideAnnotationsError" title="No annotations">
        <GInfo error>
            <p>None of the documents have annotations. Either:</p>
            <ul>
                <li>
                    <GNav :route="{ path: '/annotate/documents' }">Upload documents</GNav> to this corpus that
                    contain source annotations
                </li>
                <li>
                    <GNav :route="{ path: '/annotate/jobs' }">Start a tagger job</GNav> to create annotations
                </li>
                <li>Or wait for an existing job to finish</li>
            </ul>
        </GInfo>
    </GCard>

    <!-- content -->
    <GCard v-else>
        <template v-if="$slots.title" #title>
            <slot name="title"></slot>
        </template>
        <template v-if="$slots.help" #help>
            <slot name="help"></slot>
        </template>
        <slot></slot>
    </GCard>
</template>

<script setup lang="ts">
// Libraries & stores
import stores from "@/stores"

// Stores
const corporaStore = stores.useCorpora()
const documentsStore = stores.useDocuments()
const jobsStore = stores.useJobs()
const jobSelectionStore = stores.useJobSelection()
const userStore = stores.useUser()

// Props
const props = defineProps({
    hideDocsError: {
        type: Boolean,
        default: false
    },
    hideCorpusError: {
        type: Boolean,
        default: false
    },
    hideAnnotationsError: {
        type: Boolean,
        default: false
    },
    hidePermissionsError: {
        type: Boolean,
        default: true
    }
})
</script>
