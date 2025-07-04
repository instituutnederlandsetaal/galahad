<!-- AnnotateTab takes care of error messages for unselected and empty corpora for the subtabs under annotate/* -->
<template>
    <!-- This div fills the background color. -->
    <!-- loading corpora-->
    <GCard v-if="corporaStore.loading && !hideCorpusError" title="Loading corpora">
        <GSpinner />
    </GCard>
    <!-- No corpus selected -->
    <GCard v-else-if="!corporaStore.corpus && !hideCorpusError" title="No corpus selected">
        <GInfo error>
            <p>No corpus has been selected.</p>
            <router-link to="/annotate/corpora">Select a corpus</router-link>
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
            <router-link to="/annotate/documents">Upload documents to this corpus</router-link>
        </GInfo>
    </GCard>
    <!-- Loading jobs -->
    <GCard v-else-if="jobsStore.loading && !hideAnnotationsError" title="Loading jobs">
        <GSpinner />
    </GCard>
    <!-- No non-empty jobs-->
    <GCard v-else-if="jobSelectionStore.options.length == 0 && !hideAnnotationsError" title="No annotations">
        <GInfo error>
            <p>None of the documents have annotations. Either:</p>
            <ul>
                <li>
                    <router-link to="/annotate/documents">Upload documents</router-link> to this corpus that contain
                    source annotations
                </li>
                <li><router-link to="/annotate/jobs">Start a tagger job</router-link> to create annotations</li>
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
import stores from "@/stores"

const corporaStore = stores.useCorpora()
const documentsStore = stores.useDocuments()
const jobsStore = stores.useJobs()
const jobSelectionStore = stores.useJobSelection()

const { hideDocsError, hideCorpusError, hideAnnotationsError } = defineProps<{
    hideDocsError?: boolean
    hideCorpusError?: boolean
    hideAnnotationsError?: boolean
}>()
</script>
