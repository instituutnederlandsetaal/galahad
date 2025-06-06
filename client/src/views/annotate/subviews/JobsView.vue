<template>
    <AnnotateTab hideAnnotationsError>
        <GTable :title="`Jobs for corpus ${corporaStore.activeCorpus?.name}`" helpLink="jobs" :columns
            :items="displayJobs" :loading="jobsStore.loading" fill hoverRow sortColumn="id" class="jobsview">
            <template #help>
                <JobsHelp />
            </template>
            <template #table-empty>
                <p v-if="Object.keys(jobsStore.taggableJobs).length">No results for current filter settings</p>
                <div v-else>No taggers showed up? Something went wrong! Please contact support.</div>
            </template>

            <!-- id cell -->
            <template #cell-id="d">
                <ExternalLink v-if="d.item.tagger.id !== SOURCE_LAYER"
                    :href="`/galahad/overview/taggers#${d.item.tagger.id}`">
                    {{ d.item.tagger.id }}
                </ExternalLink>
                <div v-else>
                    <span style="font-weight: bold">{{ d.item.tagger.id }}</span>
                </div>
            </template>

            <!-- annotations cell -->
            <template #cell-annotations="d">
                {{ d.item.tagger.annotations.join(", ") }}
                <i v-if="d.item.tagger.annotations.length === 0">None</i>
            </template>

            <!-- result summary cell -->
            <template #cell-resultSummary="d">
                <!-- <span v-for="key in Object.keys(d.value)" :key><span :key v-if="d.value[key] > 0">{{ key }}: {{ d.value[key] }}, </span></span> -->
                {{ d.value.tokens }}
            </template>

            <!-- era cell -->
            <template #cell-era="d">
                <div style="white-space: nowrap">
                    <b v-if="eraRange[0] <= d.item.tagger.eraFrom">{{ d.item.tagger.eraFrom }}</b><span v-else>{{
                        d.item.tagger.eraFrom }}</span>
                    &ndash;
                    <b v-if="eraRange[1] >= d.item.tagger.eraTo">{{ d.item.tagger.eraTo }}</b><span v-else>{{
                        d.item.tagger.eraTo }}</span>
                </div>
            </template>

            <!-- modified cell -->
            <template #cell-modified="d">
                <span style="white-space: nowrap">{{ formatDate(d.item.modified) }}</span>
            </template>

            <!-- progress cell -->
            <template #cell-progress="d">
                <span>
                    <!-- note that percentage is calculated based on num documents, ie not very accurate -->
                    {{ d.item.progress.total === 0 ? "0%" : formatProgress(d.item.progress) }}
                    <span v-if="d.item.progress.hasError" style="color: var(--int-red)">error !!</span>
                </span>
                <GSpinner class="spinner" small v-show="d.item.progress.busy" />
            </template>

            <!-- actions cell -->
            <template v-slot:cell-actions="d">
                <GButton @click="jobId = d.item.tagger.id"> View &amp; Tag </GButton>
            </template>

            <template #header>
                <div class="table-controls">
                    <div class="table-control">
                        Search tagger name:
                        <GInput type="text" v-model="taggerNameFilter" placeholder="Tagger name" clearBtn></GInput>
                    </div>

                    <div class="table-control">
                        Tagset, any of:
                        <div v-for="tagset in tagsets" :key="tagset">
                            <GCheckBox v-model="includeTagset[tagset]">
                                {{ tagset || "Unknown" }}
                            </GCheckBox>
                        </div>
                    </div>

                    <div class="table-control">
                        Require annotation:
                        <MultiSelect v-model="requireType" :options="types" placeholder="Annotation"
                            :maxSelectedLabels="5" />
                    </div>
                </div>

            </template>
        </GTable>

        <!-- job modal -->
        <JobModal v-if="jobId" :jobId @hide="jobId = null" />
    </AnnotateTab>
</template>

<script setup lang="ts">
// Libraries & stores

// import VueSlider from "vue-slider-component"
// import "vue-slider-component/theme/default.css"
import stores from "@/stores"
// API & types
import { type Job, type Progress, SOURCE_LAYER } from "@/types/jobs"
import type { Column } from "@/types/ui/table"

import { formatDate } from "@/ts/utils"
import MultiSelect from "primevue/multiselect"

// Stores
const userStore = stores.useUser()
const jobsStore = stores.useJobs()
const corporaStore = stores.useCorpora()

// Fields
const taggerNameFilter = ref("")
const includeTagset = ref({} as { [tagset: string]: boolean })
const requireType = ref([])
const eraRange = ref([500, 2050])
const jobId = ref(null as null | string)

const displayJobs = computed(() =>
    Object.values(jobsStore.taggableJobs as Job[])
        .filter(job => {
            // Case insensitive string comparison.
            return job.tagger.id
                .toLowerCase()
                .includes(taggerNameFilter.value.toLowerCase())
        })
        .filter(job => {
            return (
                eraRange.value[0] <= job.tagger.eraTo &&
                eraRange.value[1] >= job.tagger.eraFrom
            )
        })
        .filter(job => {
            return includeTagset.value[job.tagger.tagset]
        })
        .filter(job => {
            for (const type of requireType.value) {
                if (!job.tagger.annotations.includes(type)) {
                    return false
                }
            }
            return true
        })
)

const tagsets = computed(() => {
    return Object.values(jobsStore.taggableJobs)
        .map((x: Job) => x.tagger.tagset)
        .filter((val, ind, arr) => arr.indexOf(val) === ind) // unique values
        .sort()
})

const columns = computed(() => {
    const publicFields = [
        {
            key: "id",
            label: "tagger",
            sortOn: x => x.tagger.id,
            align: "left"
        },
        { key: "tagset", sortOn: x => x.tagger.tagset },
        { key: "annotations", label: "annotations" },
        {
            key: "resultSummary",
            label: "tokens",
            align: "right",
            sortOn: x => x.resultSummary.numTokens
        },
        {
            key: "era",
            label: "period",
            sortOn: x => x.tagger.eraFrom.toString() + x.tagger.eraTo.toString()
        },
        {
            key: "modified",
            label: "modified",
            align: "center",
            sortOn: x => x.modified
        },
        {
            key: "progress",
            align: "right",
            sortOn: x => x.progress.finished / x.progress.total
        }
    ] as Column[]
    if (userStore.canWrite) {
        return publicFields.concat({ key: "actions" })
    }
    return publicFields
})

const types = computed(() => {
    return Object.values(jobsStore.taggableJobs)
        .flatMap((x: Job) => x.tagger.annotations)
        .filter((val, ind, arr) => arr.indexOf(val) === ind) // unique values
        .sort()
})

// Watches & mounts
onMounted(() => {
    jobsStore.reload()
})
watch(tagsets, enableAllTagsets)
onMounted(enableAllTagsets)

// Methods
// Checkmarks
function enableAllTagsets() {
    tagsets.value.forEach(tagset => (includeTagset.value[tagset] = true))
}
// Format progress with Math.floor, because e.g. toFixed(0) rounds up 99.9% to 100%, which is confusing.
function formatProgress(progress: Progress) {
    return `${Math.floor((100 * progress.finished) / progress.total)}%`
}
</script>

<style scoped lang="scss">
.spinner {
    position: relative;
    top: 3px;
}

/* Set a width even when there are no results after filtering.*/
:deep(#header) {
    max-width: 100%;
}

table button {
    background-color: rgba(0, 0, 0, 0);
    border: var(--int-grey) solid 1px;

    &:hover {
        background-color: rgba(0, 0, 0, 0.1);
    }

    &:active {
        background-color: rgba(0, 0, 0, 0.15);
    }
}
</style>
