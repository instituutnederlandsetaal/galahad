<template>
    <label for="job-select">{{ label }}</label>
    <GSpinner v-if="jobsStore.loading" />
    <GSelect v-else id="job-select" title="Select an annotation layer" :options="jobSelectionStore.selectableJobs"
        v-model="selectedJob" />
    <GInfo v-if="untaggedDocsExist">
        <p>
            Not all documents have been tagged yet. It is still possible to select this layer, but it will be
            incomplete. Alternatively, <GNav :route="{ path: '/annotate/jobs' }">start a new tagger job</GNav> or wait
            for the current job to finish.
        </p>
    </GInfo>
    <GInfo v-if="sourceLayerHasMissingAnnotations">
        <p>
            Some documents in this corpus have no source annotations. It is still possible to select this layer, but it
            will be incomplete. Alternatively, <GNav :route="{ path: '/annotate/documents' }">go to documents</GNav> and
            add or remove documents.
        </p>
    </GInfo>
</template>

<script setup lang="ts">
import stores from "@/stores"
import { SOURCE_LAYER } from "@/types/jobs"

const jobsStore = stores.useJobs()
const jobSelectionStore = stores.useJobSelection()
const documentsStore = stores.useDocuments()

const { isReference = false, displayName } = defineProps<{
    isReference?: boolean
    displayName?: string
}>()
const label = computed<string>(() => displayName ?? (isReference ? "Reference" : "Hypothesis"))
const selectedJob = computed<string>({
    get(): string {
        return isReference
            ? jobSelectionStore.referenceJobId
            : jobSelectionStore.hypothesisJobId
    },
    set(newValue: string): void {
        if (isReference) {
            jobSelectionStore.referenceJobId = newValue
        } else {
            jobSelectionStore.hypothesisJobId = newValue
        }
    },
})

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
</script>
