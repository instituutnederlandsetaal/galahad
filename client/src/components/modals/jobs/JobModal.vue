<template>
    <GModal :title="`Tag job ${job.tagger.id}`" @hide="$emit('hide')">
        <template #help>
            <p>
                Here you can start a job to tag the documents in your corpus. This may take a while, depending on the
                corpus size. You can also stop and delete existing jobs. A preview of the resulting annotation layer is
                shown as well.
            </p>
            <p>
                The tagger status (pending, busy, error, finished) will be displayed in the status bar. Tagging is
                carried out in the background. You do not need to keep the application open. There is also an indication
                of how busy the server is.
            </p>
        </template>

        <!-- Loading screen -->
        <template v-if="taggerIsAvailable == null">
            <GSpinner />
            <p>Connecting to tagger...</p>
        </template>

        <!-- Content -->

        <template v-else>
            <GInfo v-if="taggerIsAvailable === false" error>
                The tagger is currently unavailable. Please try again later.
            </GInfo>

            <!-- Job duration -->
            <template v-else>
                <p class="centerText" v-if="job.progress.untagged > 0">
                    <template v-if="jobIndication != null">
                        GaLAHaD is currently processing <b>{{ jobIndication }}</b>
                        {{ jobIndication == 1 ? "job" : "jobs" }}
                    </template>
                    <template v-else>Calculating current server load...</template>
                </p>
            </template>

            <!-- Show load icon while posting an action-->
            <GSpinner v-if="jobsStore.posting" />

            <GForm v-else-if="taggerIsAvailable" gap="0.25rem">
                <GButton
                    green
                    :disabled="job.progress.pending === 0 || job.progress.busy"
                    @click="
                        () => {
                            jobsStore.tag(job.tagger.id)
                            healthLoading = true
                        }
                    "
                >
                    Start
                </GButton>
                <GButton
                    orange
                    :disabled="!job.progress.busy"
                    @click="
                        () => {
                            jobsStore.cancel(job.tagger.id)
                            healthLoading = true
                        }
                    "
                >
                    Stop
                </GButton>
                <GButton
                    red
                    :disabled="job.progress.untagged === job.progress.total && !job.progress.hasError"
                    @click="deleteJobId = job.tagger.id"
                >
                    Delete
                </GButton>
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

            <!-- Layer preview -->
            <LayerViewer v-if="job.annotations.token > 0" :job />
        </template>

        <!-- delete job modal -->
        <DeleteModal
            v-if="deleteJobId"
            :itemName="`the results of job ${deleteJobId}`"
            @delete="
                () => {
                    jobsStore.remove(deleteJobId)
                    healthLoading = true
                }
            "
            @hide="deleteJobId = undefined"
        />
    </GModal>
</template>

<script setup lang="ts">
import * as API from "@/api/taggers"
import stores from "@/stores"
import type { Job } from "@/types/jobs"
import { type TaggerHealth, TaggerStatus } from "@/types/taggers"

// Stores
const errors = stores.useErrors()
const jobsStore = stores.useJobs()

// Fields
const { jobId } = defineProps<{ jobId: string }>()
/** The job of this modal */
const job = computed<Job>((): Job => jobsStore.jobs.find((j: Job) => j.tagger.name === jobId))
/** Returns null while we are waiting on the first getHealth request. */
const taggerIsAvailable = computed<boolean | null>(() => {
    if (!health.value) return null
    return health.value?.status === TaggerStatus.HEALTHY
})
/** Opens DeleteModal when not null. */
const deleteJobId = ref<string | null>(null)
/** Expected job duration based on queue size at tagger and % of documents tagged in the corpus. */
const jobIndication = computed<number | null>(() => {
    if (jobsStore.posting || jobsStore.queueSize == null) {
        return null
    }
    return jobsStore.queueSize
})
/** Updated on an interval to keep track of the queue size. */
const health = ref<TaggerHealth | null>(null)
/** When true, display job duration as still calculating. */
const healthLoading = ref<boolean>(true)
/** Keep track of the interval id, we stop polling on modal close. */
let healthIntervalId = 0

// Watches & mounts
/**
 * Every time this GModal opens: One health ping now, the rest on an interval.
 */
onMounted(() => {
    getHealth()
    healthIntervalId = setInterval(getHealth, 5000)
    // Set to null to induce 'calculating' every time the modal opens.
    jobsStore.getDocsAtTagger()
})
/**
 * Stop pinging health on modal close.
 */
onUnmounted(() => {
    clearInterval(healthIntervalId)
})

// Methods
/**
 * Get the health of the tagger. Called every 5 seconds while the modal is open.
 * Starting and stopping sets health loading to true. getHealth sets it back to false.
 */
function getHealth() {
    API.getTaggerHealth(jobId)
        .then((response) => {
            health.value = response.data
            healthLoading.value = false
        })
        .catch((error) => errors.handle(error))
}

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
