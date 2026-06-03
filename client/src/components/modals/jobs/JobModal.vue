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
            <!--Unhealthy-->
            <template v-if="!healthy">
                <GInfo error> The tagger is currently unavailable. Please try again later. </GInfo>
            </template>
            <!--Healthy-->
            <template v-else>
                <!--Posting-->
                <template v-if="posting">
                    <!-- Show load icon while posting an action-->
                    <GSpinner />
                </template>
                <!-- Actions -->
                <template v-else>
                    <TaggerQueue />

                    <GForm gap="0.25rem">
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
                        <DeleteButton
                            :disabled="!job.progress.finished && !job.progress.failed"
                            @click="deleteJob = job"
                        />
                    </GForm>

                    <JobProgressBar :job />

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
                        <div v-if="job.progress.failed > 5">
                            ... and {{ job.progress.failed - 5 }} more errors are omitted.
                        </div>
                        <div v-if="job.progress.failed === 0">None</div>
                    </GInfo>

                    <LayerViewer v-if="layer?.preview?.terms" :layer />
                </template>
            </template>
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
import useLayers from "@/stores/layers"
import type { Job } from "@/types/jobs"

// Stores
const { posting, jobs } = storeToRefs(useJobs())
const { layers } = storeToRefs(useLayers())
const { tag, cancel, remove } = useJobs()

// Fields
const { jobId } = defineProps<{ jobId: string }>()
const job = computed<Job>(() => jobs.value.find((j: Job) => j.tagger.name == jobId))
const layer = computed<LayerMetadata>(() => layers.value.find((l: LayerMetadata) => l.tagger.name == jobId))
/** Opens DeleteModal when not null. */
const deleteJob = ref<Job>()
/** Initial health call. */
const healthy = ref<boolean>()
/** When true, display job duration as still calculating. */
const healthLoading = ref<boolean>(true)

onMounted(() => {
    API.getTaggerHealth(job.value.tagger.name)
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
