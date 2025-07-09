<template>
    <AnnotateTab hideAnnotationsError>
        <GTable :columns :items :loading :title="`Jobs for corpus ${corpus.name}`" helpLink="jobs" sortColumn="id">
            <template #help>
                <JobsHelp />
            </template>

            <template #header>
                <GForm>
                    <fieldset>
                        <label for="tagger-name">Search tagger name</label>
                        <GInput id="tagger-name" type="text" v-model="taggerFilter" placeholder="Tagger name"></GInput>
                    </fieldset>

                    <fieldset>
                        <label for="tagset-select">Select tagset</label>
                        <MultiSelect
                            id="tagset-select"
                            v-model="tagsetFilter"
                            :options="tagsets"
                            placeholder="Tagset"
                        />
                    </fieldset>

                    <fieldset>
                        <label for="annotation-select">Select annotation</label>
                        <MultiSelect
                            id="annotation-select"
                            v-model="annotationFilter"
                            :options="annotations"
                            placeholder="Annotation"
                            :maxSelectedLabels="5"
                        />
                    </fieldset>
                </GForm>
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
                <GSpinner v-if="d.item.progress.busy" small class="spinner" />
                <span v-if="d.item.progress.hasError" class="error">error</span>
                {{ d.item.progress.total === 0 ? "0%" : formatProgress(d.item.progress) }}
            </template>

            <!-- actions cell -->
            <template v-slot:cell-actions="d: TableData<Job>">
                <GButton plain @click="jobId = d.item.tagger.id">View &amp; Tag</GButton>
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

// #stores
const { canWrite } = storeToRefs(stores.useUser())
const { loading, taggerJobs } = storeToRefs(stores.useJobs())
const { corpus } = storeToRefs(stores.useCorpora())

// #data
const jobId = ref<string>()
// filters
const taggerFilter = ref<string>("")
const tagsetFilter = ref<string[]>([])
const annotationFilter = ref<string[]>([])

// #computed
// select options
const tagsets = computed<string[]>(() => [...new Set(taggerJobs.value.flatMap((j: Job) => j.tagger.tagset))])
const annotations = computed<string[]>(() => [...new Set(taggerJobs.value.flatMap((j: Job) => j.tagger.annotations))])
// table data
const items = computed(() =>
    taggerJobs.value
        .filter((j: Job) =>
            // Case insensitive string comparison.
            j.tagger.id.toLowerCase().includes(taggerFilter.value.toLowerCase()),
        )
        .filter((j: Job) => (tagsetFilter.value?.length > 0 ? tagsetFilter.value.includes(j.tagger.tagset) : true))
        .filter((j: Job) =>
            annotationFilter.value?.length > 0
                ? j.tagger.annotations.some((a: string) => annotationFilter.value.includes(a))
                : true,
        ),
)
const columns = computed<Column<Job>[]>((): Column<Job>[] => [
    { key: "id", label: "tagger", sortOn: (j: Job): string => j.tagger.id, align: "left" },
    { key: "tagset", sortOn: (j: Job): string => j.tagger.tagset, format: (j: Job): string => j.tagger.tagset },
    { key: "language", sortOn: (j: Job): string => j.tagger.language, format: (j: Job): string => j.tagger.language },
    {
        key: "annotations",
        format: (j: Job): string => j.tagger.annotations.join(", "),
        sortOn: (j: Job): string => j.tagger.annotations.join(),
    },
    {
        key: "tokens",
        align: "right",
        sortOn: (j: Job): number => j.summary.tokens,
        format: (j: Job): number => j.summary.tokens,
    },
    {
        key: "period",
        sortOn: (j: Job): string => `${j.tagger.eraFrom} ${j.tagger.eraTo}`,
        format: (j: Job): string => `${j.tagger.eraFrom} – ${j.tagger.eraTo}`,
    },
    { key: "modified", align: "center", format: (j: Job): string => formatDate(j.modified) },
    { key: "progress", align: "right", sortOn: (j: Job): number => j.progress.finished / j.progress.total },
    { key: "actions", hidden: !canWrite.value },
])

// #methods
function formatProgress(progress: Progress): string {
    // Format progress with Math.floor, because e.g. toFixed(0) rounds up 99.9% to 100%, which is confusing.
    return `${Math.floor((100 * progress.finished) / progress.total)}%`
}
</script>

<style scoped lang="scss">
.error {
    color: var(--int-red);
}

.spinner {
    display: inline-block;
    padding-right: 0.5rem;
    padding-top: 0.25rem;
}
</style>
