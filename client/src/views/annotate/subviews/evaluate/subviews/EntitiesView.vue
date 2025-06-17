<template>
    <GCard title="Entities">
        <template #help>
            <p>
                Here you can see all the named entities in all jobs.
            </p>
        </template>

        <GTable class="table" :loading :items :columns sortColumn="document">
            <template #header>
                <form class="filter" @submit.prevent>
                    <fieldset>
                        <label for="entities-select">Entities</label>
                        <MultiSelect id="entities-select" v-model="selectedEntities" :options="entityOptions"
                            placeholder="Select entities" :maxSelectedLabels="3" />
                    </fieldset>
                    <fieldset>
                        <label for="jobs-select">Jobs</label>
                        <MultiSelect id="jobs-select" v-model="selectedJobs" :options="jobOptions"
                            placeholder="Select jobs" :maxSelectedLabels="3" />
                    </fieldset>
                </form>
            </template>
            <template #cell="data">
                <GButton v-if="data.value" class="button" @click="selectedItem = data">
                    {{ formatNumber(data.value) }}
                </GButton>
            </template>
        </GTable>

        <GModal v-if="selectedItem !== undefined" @hide="selectedItem = undefined">
            <DocumentEntitiesTable :entities="selectedItem?.item?.entities" />
        </GModal>
    </GCard>
</template>

<script setup lang="ts">
import stores from "@/stores"
import type { Column, TableData } from "@/types/ui/table"
import type { JobsEntities } from "@/types/evaluation/entities"
import MultiSelect from "primevue/multiselect"


// --- types ---
type DocumentEntitiesRow = {
    document: string
    [key: string]: number | string
}

// --- stores ---
const { entities, loading } = storeToRefs(stores.useEntities())

// --- data ---
const selectedItem = ref<TableData<any>>()
const selectedEntities = ref<string[]>([])
const selectedJobs = ref<string[]>([])

// --- computed ---
const entityOptions = computed(() => ["total"].concat(Object.keys(entities.value?.stddev?.stddev ?? {}).toSorted()))
const jobOptions = computed(() => Object.keys(entities.value?.jobs ?? {}).toSorted())
const items = computed<DocumentEntitiesRow[]>(() => convertJobsEntities(entities.value))
const columns = computed<Column<Record<string, number>>[]>(() => {
    if (!entities.value) {
        return []
    }
    const cols: Column<DocumentEntitiesRow>[] = [
        { key: "document", sortOn: (d: DocumentEntitiesRow): number | string => d.document === "total" ? Number.POSITIVE_INFINITY : d.document },
        { key: "entities", hidden: true },
    ]

    const jobLabels = []
    for (const [jobName, jobData] of Object.entries(entities.value.jobs)) {
        if (filterAccepts("total", jobName)) {
            jobLabels.push({ key: `${jobName} total` })
        }
        for (const [eLabel, _] of Object.entries(jobData.summary)) {
            if (filterAccepts(eLabel, jobName)) {
                jobLabels.push({ key: `${jobName} ${eLabel}` })
            }
        }
    }
    cols.push(...jobLabels.toSorted((a, b) => a.key.localeCompare(b.key)))

    const stdLabels = []
    for (const [eLabel, _] of Object.entries(entities.value.stddev.stddev)) {
        if (filterAccepts(eLabel)) {
            stdLabels.push({ key: `${eLabel} std` })
        }
    }
    cols.push(...stdLabels.toSorted((a, b) => a.key.localeCompare(b.key)))

    cols.push({ key: "stdavg" })
    return cols
})

// --- methods ---
function filterAccepts(entity: string, job?: string): boolean {
    // none active
    if (selectedEntities.value.length === 0 && selectedJobs.value.length === 0) return true
    // only one active
    if (selectedEntities.value.length === 0) return selectedJobs.value.includes(job)
    if (selectedJobs.value.length === 0) return selectedEntities.value.includes(entity)
    // both active
    return selectedEntities.value.includes(entity) && (!job || selectedJobs.value.includes(job))
}

function convertJobsEntities(
    jobsEntities: JobsEntities
): DocumentEntitiesRow[] {
    if (!jobsEntities) return []

    const result: Record<string, { [key: string]: number | string }> = {}

    // rest of the documents
    for (const [jobName, jobData] of Object.entries(jobsEntities.jobs)) {
        for (const [docName, docData] of Object.entries(jobData.documents)) {
            if (!result[docName]) {
                result[docName] = { document: docName }
            }

            for (const [eLabel, eCount] of Object.entries(docData.summary)) {
                result[docName][`${jobName} ${eLabel}`] = eCount
            }
            result[docName][`${jobName} total`] = docData.total

            if (!result[docName].entities) {
                result[docName].entities = {}
            }
            result[docName].entities[jobName] = docData.entities
        }
    }

    for (const [docName, doc] of Object.entries(
        jobsEntities.stddev.documents
    )) {
        for (const [eLabel, stddev] of Object.entries(doc.stddev)) {
            result[docName][`${eLabel} std`] = stddev
        }
        result[docName].stdavg = doc.average
    }

    // document total
    result["total"] = { document: "total" }
    for (const [jobName, jobData] of Object.entries(jobsEntities.jobs)) {
        result["total"][`${jobName} total`] = jobData.total
        for (const [eLabel, eCount] of Object.entries(jobData.summary)) {
            result["total"][`${jobName} ${eLabel}`] = eCount
        }
    }

    result["total"].stdavg = jobsEntities.stddev.average
    for (const [eLabel, stddev] of Object.entries(jobsEntities.stddev.stddev)) {
        result["total"][`${eLabel} std`] = stddev
    }

    return Object.values(result)
}

// custom format because we are overriding the cell slot
function formatNumber(value: unknown): string | unknown {
    // only tofixed if value is a float
    if (typeof value === "number" && !Number.isInteger(value)) {
        return value.toFixed(2)
    }
    return value
}

</script>

<style scoped lang="scss">
.table {
    :deep(td) {
        padding: 0 !important;
        box-sizing: border-box !important;

        .button {
            width: 100%;
            height: 100%;
            background-color: transparent;
            justify-content: right;


            &:hover {
                background-color: var(--int-light-grey) !important;
            }

            &:focus {
                background-color: var(--int-light-grey-hover) !important;
            }
        }
    }
}

.filter {
    display: flex;
    flex-wrap: wrap;
    gap: 1rem;

    fieldset {
        display: flex;
        flex-direction: column;
        align-items: center;
    }
}
</style>
