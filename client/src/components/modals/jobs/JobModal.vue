<template>
    <GModal :title="job.tagger.name" @hide="$emit('hide')">
        <template #help>
            <JobModalHelp />
        </template>

        <!-- Loading screen -->
        <template v-if="healthLoading">
            <GSpinner />
            <p>Connecting to tagger...</p>
        </template>

        <!-- Content -->

        <template v-else>
            <GInfo v-if="!healthy" error> The tagger is currently unavailable. Please try again later. </GInfo>

            <!-- Job duration -->
            <TaggerQueue />

            <!-- Show load icon while posting an action-->
            <GSpinner v-if="jobsLoading" />

            <GForm v-else-if="healthy" gap="0.25rem">
                <GButton
                    green
                    :disabled="!job.progress.untagged"
                    @click="
                        () => {
                            tag(job)
                        }
                    "
                >
                    <i class="fa fa-play"></i>
                </GButton>
                <GButton
                    orange
                    :disabled="!job.progress.processing"
                    @click="
                        () => {
                            cancel(job)
                        }
                    "
                >
                    <i class="fa fa-pause"></i>
                </GButton>
                <DeleteButton :disabled="!job.progress.finished && !job.progress.failed" @click="deleteJob = job" />
            </GForm>

            <!-- progress -->
            <JobProgress :job />

            <!-- errors -->
            <GInfo v-if="job.progress.failed > 0" error>
                The following
                {{ job.progress.failed == 1 ? "document" : "documents" }} encountered errors:<br /><br />
                <ol>
                    <li v-for="(message, doc) in firstFive(job.progress.errors)" :key="doc">
                        <b>{{ doc }}</b
                        >:<br />
                        {{ message }}
                    </li>
                </ol>
                <div v-if="job.progress.failed > 5">... and {{ job.progress.failed - 5 }} more errors are omitted.</div>
                <div v-if="job.progress.failed === 0">None</div>
            </GInfo>
        </template>

        <!-- delete job modal -->
        <DeleteModal
            v-if="deleteJob"
            :itemName="`the results of ${deleteJob.tagger.name}`"
            @delete="
                () => {
                    remove(deleteJob)
                }
            "
            @hide="deleteJob = undefined"
        />
    </GModal>
</template>

<script setup lang="ts">
import * as API from "@/api/taggers"
import useJobs from "@/stores/jobs"
import type { Job } from "@/types/jobs"

// Stores
const { loading: jobsLoading } = storeToRefs(useJobs())
const { tag, cancel, remove } = useJobs()

// Fields
const { job } = defineProps<{ job: Job }>()
/** Opens DeleteModal when not null. */
const deleteJob = ref<Job>()
/** Expected job duration based on queue size at tagger and % of documents tagged in the corpus. */
/** Updated on an interval to keep track of the queue size. */
const healthy = ref<boolean>()
/** When true, display job duration as still calculating. */
const healthLoading = ref<boolean>(true)

onMounted(() => {
    API.getTaggerHealth(job.tagger.name)
        .then((res) => {
            healthy.value = res.data
        })
        .finally(() => {
            healthLoading.value = false
        })
})

/**
 * Return an object with only the first five keys of obj.
 */
function firstFive(obj: Record<string, unknown>) {
    if (!obj) {
        return {}
    }
    return Object.keys(obj)
        .slice(0, 5)
        .reduce((r: Record<string, unknown>, e) => {
            r[e] = obj[e]
            return r
        }, {})
}
</script>
