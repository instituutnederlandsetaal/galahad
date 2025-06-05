<template>
    <label>{{ label }}</label>
    <GSpinner v-if="jobsStore.loading" />
    <GSelect v-else title="Select an annotation layer" :options="jobSelectionStore.selectableJobs"
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
const label = computed<string>(
    () => displayName ?? (isReference ? "Reference" : "Hypothesis")
)
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
    }
})

// Whether there are documents that have not been tagged yet.
// Not relevant for source layer.
const untaggedDocsExist = computed<boolean>(() => {
    if (!job.value) return false
    return job.value.progress.finished < job.value.progress.total
})
// Whether the selected layer is sourceLayer and has missing annotations.
const sourceLayerHasMissingAnnotations = computed<boolean>(() => {
    if (!job.value) return false
    return job.value.progress.finished !== documentsStore.numSourceAnnotations
})

const job = computed<Job | undefined>(() => {
    if (!selectedJob.value) return undefined
    if (!jobsStore.jobs) return undefined
    // any job could be any job, even the source layer
    const anyJob = jobsStore.jobs[selectedJob.value]
    if (!anyJob) return undefined
    // if the job is not source layer, return it
    if (anyJob.tagger.id !== SOURCE_LAYER) return anyJob
    // if the job is source layer, return undefined
    return undefined
})
</script>
