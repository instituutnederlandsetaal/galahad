<template>
    <GCard title="Corpora" helpLink="corpora">
        <template #help>
            <CorpusHelp />
        </template>

        <form class="buttons" @submit.prevent>
            <GButton green title="New" @click="newCorpus = true">
                New
            </GButton>
            <GButton orange title="Edit" :disabled="!user.canWrite" @click="editCorpus = copy(corpus)">
                Edit
            </GButton>
            <GButton red title="Delete" :disabled="!user.canDelete" @click="deleteCorpus = corpus">
                Delete
            </GButton>
        </form>

        <!-- Owner corpus table -->
        <CorpusTable :type="TableCorporaType.user" :corpora="corpora">
            <template #title>Your corpora</template>
        </CorpusTable>

        <!-- Shared corpus table -->
        <CorpusTable :type="TableCorporaType.shared" :corpora="sharedCorpora">
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
        <CorpusTable :type="TableCorporaType.dataset" :corpora="datasets">
            <template #title>Benchmark corpora</template>
            <template #help>
                <BenchmarkSetsHelp />
                <p>
                    You can inspect them in further detail on the
                    <router-link to="/annotate/evaluate">Evaluate tab</router-link>.
                </p>
            </template>
            <template #table-empty>
                No benchmark corpora available.
            </template>
        </CorpusTable>

        <!-- Create modal -->
        <CorpusForm v-if="newCorpus" title="New corpus" @hide="newCorpus = false" @confirm="create">
            <template #help>
                Fill in the metadata and create a corpus.
                <CorpusFormHelp />
            </template>
        </CorpusForm>

        <!-- Update modal -->
        <CorpusForm v-if="editCorpus" title="Edit corpus" :initial="editCorpus" @hide="editCorpus = undefined"
            @confirm="update">
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
const { corpora, sharedCorpora, datasets, corpus } = storeToRefs(corporaStore)
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
