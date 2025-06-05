<template>
    <GCard title="Entities">
        <template #help>
            <p>
                Here you can see all the named entities in all jobs.
            </p>
        </template>

        <GTable class="table" :loading :items :columns compact>
            <template #cell="data: TableData<DocumentEntities>">
                <GButton class="button" @click="selectedItem = data">
                    {{ data.value }}
                </GButton>
            </template>
        </GTable>

        <GModal v-if="selectedItem !== undefined" @hide="selectedItem = undefined"
            :title="`Entities in ${selectedItem?.item?.document}`">
            <template #help>
                Here you can view all the entities in the selected document.
            </template>
            <DocumentEntitiesTable :entities="selectedItem?.item?.entities" />
        </GModal>
    </GCard>
</template>

<script setup lang="ts">
import stores from "@/stores"
import * as API from "@/api/evaluation"
import type { Column, TableData } from "@/types/ui/table"
import type {
    DocumentEntities,
    JobEntities,
    JobsEntities
} from "@/types/evaluation/entities"

// --- stores ---
const corpora = stores.useCorpora()

// --- data ---
const loading = ref<boolean>(false)
const items = ref<any>([])
const selectedItem = ref<TableData<any>>()

// --- computed ---
const columns = computed<Column[]>(() =>
    Object.keys(items.value[0] || {}).map(i => ({
        key: i,
        hidden: i === "entities"
    }))
)
const filter = computed(() => {
    if (["document", "total"].includes(selectedItem.value?.field.key)) {
        return undefined
    }
    return selectedItem.value?.field?.key
})

// --- watch ---
watchEffect(() => {
    loading.value = true
    API.getJobsEntities(corpora.activeUUID)
        .then(res => {
            items.value = convertJobsEntities(res.data)
        })
        .finally(() => {
            loading.value = false
        })
})

// --- methods ---
function convertJobsEntities(
    jobsEntities: JobsEntities
): { document: string; [key: string]: number | string }[] {
    const result: Record<string, { [key: string]: number | string }> = {}

    // rest of the documents
    for (const [jobName, jobData] of Object.entries(jobsEntities.jobs)) {
        for (const [docName, docData] of Object.entries(jobData.documents)) {
            for (const [eLabel, eCount] of Object.entries(docData.summary)) {
                if (!result[docName]) {
                    result[docName] = { document: docName }
                }
                result[docName][`${jobName}-${eLabel}`] = eCount
            }
            result[docName][`${jobName}-total`] = docData.total

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
            result[docName][`${eLabel}-std`] = stddev.toFixed(2)
        }
        result[docName].stdavg = doc.average.toFixed(2)
    }

    // document total
    result["total"] = { document: "total" }
    for (const [jobName, jobData] of Object.entries(jobsEntities.jobs)) {
        result["total"][`${jobName}-total`] = jobData.total
        for (const [eLabel, eCount] of Object.entries(jobData.summary)) {
            result["total"][`${jobName}-${eLabel}`] = eCount
        }
    }

    result["total"].stdavg = jobsEntities.stddev.average.toFixed(2)
    for (const [eLabel, stddev] of Object.entries(jobsEntities.stddev.stddev)) {
        result["total"][`${eLabel}-std`] = stddev.toFixed(2)
    }

    return Object.values(result)
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

            &:hover {
                background-color: var(--int-light-grey) !important;
            }

            &:focus {
                background-color: var(--int-light-grey-hover) !important;
            }
        }
    }
}
</style>
