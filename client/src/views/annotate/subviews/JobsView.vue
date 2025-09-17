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

            <!-- annotations cell -->
            <template #cell-annotations="d: TableData<Job>">
                <AnnotationItemsViewer :items="d.item.tagger.annotations">
                    <template #title>Annotations and principles of {{ d.item.tagger.id }}</template>
                </AnnotationItemsViewer>
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
import AnnotationItemsViewer from "@/components/modals/metadata/AnnotationItemsViewer.vue"

// #stores
const { canWrite } = storeToRefs(stores.useUser())
const { loading, taggerJobs } = storeToRefs(stores.useJobs())
const { reload } = stores.useJobs()
const { corpus } = storeToRefs(stores.useCorpora())

reload()

// #data
const jobId = ref<string>()
// filters
const taggerFilter = ref<string>("")
const tagsetFilter = ref<string[]>([])
const annotationFilter = ref<string[]>([])

// #computed
// select options
const annotations = computed<string[]>(() => [
    ...new Set(taggerJobs.value.flatMap((j: Job) => j.tagger.annotations.flatMap((a) => a.annotation))),
])
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
    { key: "language", sortOn: (j: Job): string => j.tagger.language, format: (j: Job): string => j.tagger.language },
    {
        key: "period",
        sortOn: (j: Job): string => `${j.tagger.period.from} ${j.tagger.period.to}`,
        format: (j: Job): string => `${j.tagger.period.from} – ${j.tagger.period.to}`,
    },
    { key: "annotations", sortOn: (j: Job): string => j.tagger.annotations.map((a) => a.annotation).join() },
    {
        key: "summary",
        label: "tokens",
        align: "right",
        sortOn: (j: Job): number => j.summary.annotations.token,
        format: (j: Job): number => j.summary.annotations.token ?? 0,
    },
    { key: "modified", align: "center", format: (j: Job): string => formatDate(j.modified) },
    { key: "progress", align: "right", sortOn: (j: Job): number => j.progress.finished / j.progress.total },
    { key: "actions", hidden: !canWrite.value, noSort: true },
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
