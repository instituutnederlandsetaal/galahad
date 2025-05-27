<template>
    <label for="job-select">{{ title }}</label>
    <GSpinner v-if="jobsStore.loading" />
    <GSelect v-else id="job-select" :options="jobSelectionStore.selectableJobs" v-model="selectedJob" />
    <GInfo v-if="untaggedDocsExist">
        Not all documents have been tagged yet. It is still possible to select this layer, but it will be
        incomplete. <br />
        Alternatively,
        <GNav :route="{ path: '/annotate/jobs' }">start a new tagger job</GNav> or wait for the current job to
        finish.
    </GInfo>
    <GInfo v-if="sourceLayerHasMissingAnnotations">
        Some documents in this corpus have no source annotations. It is still possible to select this layer, but it
        will be incomplete. <br />
        Alternatively, <GNav :route="{ path: '/annotate/documents' }">go to documents</GNav> and remove or add
        documents.
    </GInfo>
</template>

<script setup lang="ts">
import stores from "@/stores"
// API & Types
import { SOURCE_LAYER } from "@/types/jobs"

// Stores
const jobsStore = stores.useJobs()
const jobSelectionStore = stores.useJobSelection()
const documentsStore = stores.useDocuments()

// Fields
const props = defineProps({
    isReference: { default: false },
    customTitle: { default: null as string | null },
})
const title = computed<string>(
    () => props.customTitle ?? (props.isReference ? "Reference" : "Hypothesis"),
)
const selectedJob = ref<string>()
// Whether there are documents that have not been tagged yet.
// Not relevant for source layer.
const untaggedDocsExist = computed(() => {
    if (!selectedJob.value) return false
    if (!jobsStore.jobs) return false
    const job = jobsStore.jobs[selectedJob.value]
    if (!job) return false
    if (job.tagger.id === SOURCE_LAYER) return false
    return job.progress.finished < job.progress.total
})
// Whether the selected layer is sourceLayer and has missing annotations.
const sourceLayerHasMissingAnnotations = computed(() => {
    if (!selectedJob.value) return false
    if (!jobsStore.jobs) return false
    const job = jobsStore.jobs[selectedJob.value]
    if (!job) return false
    if (job.tagger.id !== SOURCE_LAYER) return false
    return job.progress.finished !== documentsStore.numSourceAnnotations
})

// Watches & mounts
// watch both referenceJobId and hypothesisJobId
watch(
    () => [jobSelectionStore.referenceJobId, jobSelectionStore.hypothesisJobId],
    () => {
        if (props.isReference)
            selectedJob.value = jobSelectionStore.referenceJobId
        else selectedJob.value = jobSelectionStore.hypothesisJobId
    },
    { immediate: true },
)
// reverse
watch(
    selectedJob,
    () => {
        if (!selectedJob.value) return
        if (props.isReference)
            jobSelectionStore.referenceJobId = selectedJob.value
        else jobSelectionStore.hypothesisJobId = selectedJob.value
    },
    { immediate: true },
)
</script>
