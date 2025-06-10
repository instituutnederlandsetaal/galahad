<template>
    <AnnotateTab hideAnnotationsError>
        <GTable :title="`Jobs for corpus ${corpus.name}`" helpLink="jobs" :columns :items :loading sortColumn="id">

            <template #help>
                <JobsHelp />
            </template>

            <template #header>
                <form class="table-controls">
                    <fieldset class="table-control">
                        <label>Search tagger name:</label>
                        <GInput type="text" v-model="taggerNameFilter" placeholder="Tagger name" clearBtn></GInput>
                    </fieldset>

                    <fieldset class="table-control">
                        <label>Select tagset:</label>
                        <MultiSelect v-model="includeTagsets" :options="tagsets" placeholder="Tagset" />
                    </fieldset>

                    <fieldset class="table-control">
                        <label>Select annotation:</label>
                        <MultiSelect v-model="includeAnnotations" :options="annotations" placeholder="Annotation"
                            :maxSelectedLabels="5" />
                    </fieldset>
                </form>
            </template>

            <template #table-empty>
                <p v-if="taggerJobs.length">No results for current filter settings</p>
                <div v-else>No taggers showed up? Something went wrong! Please contact support.</div>
            </template>

            <!-- id cell -->
            <template #cell-id="d: TableData<Job>">
                <ExternalLink :href="`/galahad/overview/taggers#${d.item.tagger.id}`">
                    {{ d.item.tagger.id }}
                </ExternalLink>
            </template>

            <!-- progress cell -->
            <template #cell-progress="d: TableData<Job>">
                <GSpinner class="spinner" small v-show="d.item.progress.busy" />
                <span>
                    <!-- note that percentage is calculated based on num documents, ie not very accurate -->
                    {{ d.item.progress.total === 0 ? "0%" : formatProgress(d.item.progress) }}
                    <span v-if="d.item.progress.hasError" style="color: var(--int-red)">error !!</span>
                </span>
            </template>

            <!-- actions cell -->
            <template v-slot:cell-actions="d: TableData<Job>">
                <GButton plain @click="jobId = d.item.tagger.id"> View &amp; Tag </GButton>
            </template>

        </GTable>

        <!-- job modal -->
        <JobModal v-if="jobId" :jobId @hide="jobId = undefined" />
    </AnnotateTab>
</template>

<script setup lang="ts">
import stores from "@/stores"
import type { Job, Progress } from "@/types/jobs"
import type { Column, TableData } from "@/types/ui/table"
import { formatDate } from "@/ts/utils"
import MultiSelect from "primevue/multiselect"

const { canWrite } = storeToRefs(stores.useUser())
const { loading, taggerJobs } = storeToRefs(stores.useJobs())
const { corpus } = storeToRefs(stores.useCorpora())

const jobId = ref<string>()
// filters
const taggerNameFilter = ref<string>("")
const annotations = computed<string[]>(() => [
    ...new Set(taggerJobs.value.flatMap((j: Job) => j.tagger.annotations))
])
const tagsets = computed<string[]>(() => [
    ...new Set(taggerJobs.value.flatMap((j: Job) => j.tagger.tagset))
])
const includeTagsets = ref<string[]>([])
const includeAnnotations = ref<string[]>([])

const items = computed(() =>
    taggerJobs.value
        .filter((j: Job) =>
            // Case insensitive string comparison.
            j.tagger.id
                .toLowerCase()
                .includes(taggerNameFilter.value.toLowerCase())
        )
        .filter((j: Job) =>
            includeTagsets.value?.length > 0
                ? includeTagsets.value.includes(j.tagger.tagset)
                : true
        )
        .filter((j: Job) =>
            includeAnnotations.value?.length > 0
                ? j.tagger.annotations.some((a: string) =>
                      includeAnnotations.value.includes(a)
                  )
                : true
        )
)

const columns = computed<Column<Job>[]>((): Column<Job>[] => [
    {
        key: "id",
        label: "tagger",
        sortOn: (j: Job): string => j.tagger.id,
        align: "left"
    },
    {
        key: "tagset",
        sortOn: (j: Job): string => j.tagger.tagset,
        format: (j: Job): string => j.tagger.tagset
    },
    {
        key: "annotations",
        format: (j: Job): string => j.tagger.annotations.join(", "),
        sortOn: (j: Job): string => j.tagger.annotations.join()
    },
    {
        key: "tokens",
        align: "right",
        sortOn: (j: Job): number => j.resultSummary.tokens,
        format: (j: Job): number => j.resultSummary.tokens
    },
    {
        key: "period",
        sortOn: (j: Job): string => `${j.tagger.eraFrom} ${j.tagger.eraTo}`,
        format: (j: Job): string => `${j.tagger.eraFrom} – ${j.tagger.eraTo}`
    },
    {
        key: "modified",
        align: "center",
        format: (j: Job): string => formatDate(j.modified)
    },
    {
        key: "progress",
        align: "right",
        sortOn: (j: Job): number => j.progress.finished / j.progress.total
    },
    {
        key: "actions",
        hidden: !canWrite.value
    }
])

// Format progress with Math.floor, because e.g. toFixed(0) rounds up 99.9% to 100%, which is confusing.
function formatProgress(progress: Progress): string {
    return `${Math.floor((100 * progress.finished) / progress.total)}%`
}
</script>

<style scoped lang="scss">
.spinner {
    position: relative;
    top: 3px;
}

.table-controls {
    display: flex;
    gap: 1rem;

    .table-control {
        display: flex;
        flex-direction: column;
    }
}
</style>
