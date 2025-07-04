<template>
    <fieldset>
        <label :for="`${label}-select`">{{ label }}</label>
        <GSpinner v-if="loading" />
        <GSelect v-else :id="`${label}-select`" :options title="Select annotation layer" v-model="jobId" />
        <GInfo v-if="untaggedDocsExist">
            <p>
                Not all documents have been tagged yet. It is still possible to select this layer, but it will be
                incomplete. Alternatively, <router-link to="/annotate/jobs">start a new tagger job</router-link> or wait
                for the current job to finish.
            </p>
        </GInfo>
    </fieldset>
</template>

<script setup lang="ts">
import stores from "@/stores"
import type { Job } from "@/types/jobs"

// #props
const { isReference = false, displayName } = defineProps<{ isReference?: boolean; displayName?: string }>()

// #stores
const { loading, jobs } = storeToRefs(stores.useJobs())
const { hypothesisId, referenceId, options } = storeToRefs(stores.useJobSelection())

// #computed
const label = computed<string>(() => displayName ?? (isReference ? "Reference" : "Hypothesis"))
const jobId = computed<string>({
    get(): string {
        return isReference ? referenceId.value : hypothesisId.value
    },
    set(newValue: string): void {
        if (isReference) {
            referenceId.value = newValue
        } else {
            hypothesisId.value = newValue
        }
    },
})
const job = computed<Job | undefined>(() => jobs.value.find((j: Job) => j.tagger.id === jobId.value))
const untaggedDocsExist = computed<boolean>(() =>
    job.value ? job.value.progress.finished < job.value.progress.total : false,
)
</script>
