<template>
    <GCard title="Entities">
        <template #help>
            <p>Here you can see all the named entities in all jobs.</p>
        </template>

        <GTable class="table" :loading :items :columns sortColumn="document">
            <template #header>
                <legend class="entities-legend">
                    <p>Legend:</p>
                    <div>
                        <span v-for="(job, i) in Object.keys(entities?.jobs ?? {})" :key="i">
                            <strong>{{ i + 1 }}:</strong> {{ job }}
                        </span>
                    </div>
                </legend>
                <GForm>
                    <fieldset>
                        <label for="entities-select">Entities</label>
                        <MultiSelect
                            id="entities-select"
                            v-model="selectedEntities"
                            :options="entityOptions"
                            placeholder="Select entities"
                            :maxSelectedLabels="3"
                            optionLabel="text"
                            optionValue="value"
                        />
                    </fieldset>
                    <fieldset>
                        <label for="jobs-select">Jobs</label>
                        <MultiSelect
                            id="jobs-select"
                            v-model="selectedJobs"
                            :options="jobOptions"
                            placeholder="Select jobs"
                            :maxSelectedLabels="3"
                        />
                    </fieldset>
                </GForm>
            </template>
            <template #cell="data">
                <GButton v-if="data.value" class="button" @click="selectedItem = data" title="View document entities">
                    {{ formatNumber(data.value) }}
                </GButton>
                <template v-else>
                    {{ formatNumber(data.value) }}
                </template>
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
type DocumentEntitiesRow = { document: string; [key: string]: number | string }

// --- stores ---
const { entities, loading } = storeToRefs(stores.useEntities())

// --- data ---
const selectedItem = ref<TableData<any>>()
const selectedEntities = ref<string[]>([])
const selectedJobs = ref<string[]>([])

// --- computed ---
const entityLegend = computed(() => {
    if (!entities.value) return {}
    const legend = {}
    for (const [jobName, jobData] of Object.entries(entities.value.jobs)) {
        legend[jobName] = Object.keys(jobData.summary)
    }
    return legend
})
const entityOptions = computed(() => {
    const options = [{ text: "Total", value: "total" }]
    const labels = Object.keys(entities.value?.stddev?.stddev ?? {}).toSorted()
    for (const label of labels) {
        // in which indices of entittyLegend does this label occur?
        const occurence = []
        for (const [i, job] of Object.keys(entities.value.jobs).entries()) {
            if (entities.value.jobs[job].summary[label] !== undefined) {
                occurence.push(i + 1) // +1 because we want to start from 1, not 0
            }
        }
        // ex.: LOC (1, 2, 3)
        const text = `${label} (${occurence})`
        options.push({ text: text, value: label })
    }
    return options
})
const jobOptions = computed(() => Object.keys(entities.value?.jobs ?? {}).toSorted())
const items = computed<DocumentEntitiesRow[]>(() => convertJobsEntities(entities.value))
const columns = computed<Column<Record<string, number>>[]>(() => {
    if (!entities.value) {
        return []
    }
    const cols: Column<DocumentEntitiesRow>[] = [
        {
            key: "document",
            sortOn: (d: DocumentEntitiesRow): number | string =>
                d.document === "total" ? Number.POSITIVE_INFINITY : d.document,
        },
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

function convertJobsEntities(jobsEntities: JobsEntities): DocumentEntitiesRow[] {
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

    for (const [docName, doc] of Object.entries(jobsEntities.stddev.documents)) {
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
    if (typeof value === "number") {
        if (!Number.isInteger(value)) {
            return value.toFixed(2)
        }
        if (value === 0) {
            return ""
        }
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

.entities-legend {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
    align-items: center;

    div {
        display: flex;
        flex-wrap: wrap;
        gap: 0.5rem;
    }
}
</style>
