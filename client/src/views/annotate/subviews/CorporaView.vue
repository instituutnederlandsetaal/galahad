<template>
    <GCard title="Corpora" helpLink="corpora">
        <template #help>
            <CorpusHelp />
        </template>

        <form class="buttons" @submit.prevent>
            <GButton green @click="newCorpus = true">
                New
            </GButton>
            <GButton orange :disabled="!user.canWrite" @click="editCorpus = copy(activeCorpus)">
                Edit
            </GButton>
            <GButton red :disabled="!user.canDelete" @click="deleteCorpus = activeCorpus">
                Delete
            </GButton>
        </form>

        <!-- Owner corpus table -->
        <CorpusTable :type="TableCorporaType.User" :corpora="allCorpora">
            <template #title>Your corpora</template>
        </CorpusTable>

        <!-- Shared corpus table -->
        <CorpusTable :type="TableCorporaType.Shared" :corpora="sharedCorpora">
            <template #title>Shared with you</template>
            <template #help>
                <p>
                    Here you can see the corpora that have been shared with you.
                    If a corpus has been shared with you as a collaborator, you can make modifications.
                    If it has been shared with you as a viewer, you can only inspect and evaluate it.
                </p>
            </template>
            <template #table-empty>
                No corpora have been shared with you.
            </template>
        </CorpusTable>

        <!-- Benchmark corpus table -->
        <CorpusTable :type="TableCorporaType.Dataset" :corpora="datasets">
            <template #title>Benchmark corpora</template>
            <template #help>
                <BenchmarkSetsHelp />
                <p>
                    You can inspect them in further detail on the
                    <GNav :route="{ path: '/annotate/evaluate' }">Evaluate tab</GNav>.
                </p>
            </template>
            <template #table-empty>
                No benchmark corpora available.
            </template>
        </CorpusTable>

        <!-- Create modal -->
        <CorpusForm v-if="newCorpus" title="New corpus" @hide="newCorpus = false"
            :action="(metadata) => { create(metadata) }">
            <template #help>
                Fill in the metadata and create a corpus.
                <CorpusFormHelp />
            </template>
        </CorpusForm>

        <!-- Update modal -->
        <CorpusForm v-if="editCorpus" title="Edit corpus" :item="editCorpus" @hide="editCorpus = null" update
            :action="(metadata) => { update(editCorpus.uuid, metadata) }">
            <template #help>
                Change the metadata of an existing corpus.
                <CorpusFormHelp />
            </template>
        </CorpusForm>

        <!-- Delete modal -->
        <DeleteModal v-if="deleteCorpus" :itemName="deleteCorpus.name" @delete="remove(deleteCorpus)"
            @hide="deleteCorpus = undefined" />
    </GCard>
</template>

<script setup lang="ts">
import stores from "@/stores"
import type { CorpusMetadata } from "@/types/corpora"
import { TableCorporaType } from "@/types/ui/table"

// Stores
const corporaStore = stores.useCorpora()
const { create, remove, update } = corporaStore
const { allCorpora, sharedCorpora, datasets, activeCorpus } =
    storeToRefs(corporaStore)
const user = stores.useUser()

// Fields
// Once not falsy, respective modal is shown.
const newCorpus = ref<boolean>()
const deleteCorpus = ref<CorpusMetadata>()
const editCorpus = ref<CorpusMetadata>()

// Deepcopy so we can modify the object freely.
function copy(corpus: CorpusMetadata): CorpusMetadata {
    return JSON.parse(JSON.stringify(corpus)) as CorpusMetadata
}
</script>

<style scoped lang="scss">
.buttons {
    display: flex;
    gap: 0.25rem;
}
</style>
