<template>
    <GCard title="Corpora" helpLink="corpora">
        <template #help>
            <CorpusHelp />
        </template>

        <!-- Action buttons -->
        <GForm gap="0.25rem">
            <GButton green title="New" @click="newCorpus = true">
                <i class="fa fa-plus"></i>
            </GButton>
            <GButton orange title="Edit" :disabled="!canWrite" @click="editCorpus = copy(corpus)">
                <i class="fa fa-pencil"></i>
            </GButton>
            <GButton red title="Delete" :disabled="!canDelete" @click="deleteCorpus = corpus">
                <i class="fa fa-trash"></i>
            </GButton>
        </GForm>

        <!-- Owner corpus table -->
        <CorpusTable :type="CorpusTableType.user" :corpora="corpora" title="Your corpora" />

        <!-- Shared corpus table -->
        <CorpusTable :type="CorpusTableType.shared" :corpora="sharedCorpora" title="Shared with you">
            <template #help>
                <p>
                    Here you can see the corpora that have been shared with you. If a corpus has been shared with you as
                    a collaborator, you can make modifications. If it has been shared with you as a viewer, you can only
                    inspect and evaluate it.
                </p>
            </template>
            <template #table-empty>No corpora have been shared with you.</template>
        </CorpusTable>

        <!-- Benchmark corpus table -->
        <CorpusTable :type="CorpusTableType.dataset" :corpora="datasets" title="Benchmark corpora">
            <template #help>
                <BenchmarkSetsHelp />
                <p>
                    You can inspect them in further detail on the
                    <router-link to="/annotate/evaluate">Evaluate tab</router-link>.
                </p>
            </template>
            <template #table-empty>No benchmark corpora available.</template>
        </CorpusTable>

        <!-- Create modal -->
        <CorpusForm v-if="newCorpus" title="New corpus" @hide="newCorpus = false" @confirm="create">
            <template #help>
                Fill in the metadata and create a corpus.
                <CorpusFormHelp />
            </template>
        </CorpusForm>

        <!-- Update modal -->
        <CorpusForm
            v-if="editCorpus"
            title="Edit corpus"
            :initial="editCorpus"
            @hide="editCorpus = undefined"
            @confirm="update"
        >
            <template #help>
                Change the metadata of an existing corpus.
                <CorpusFormHelp />
            </template>
        </CorpusForm>

        <!-- Delete modal -->
        <DeleteModal
            v-if="deleteCorpus"
            :itemName="deleteCorpus.name"
            @delete="remove(deleteCorpus)"
            @hide="deleteCorpus = undefined"
        />
    </GCard>
</template>

<script setup lang="ts">
import stores from "@/stores"
import type { CorpusMetadata } from "@/types/corpora"
import { CorpusTableType } from "@/types/ui/table"

const { create, remove, update, reload } = stores.useCorpora()
const { corpora, sharedCorpora, datasets, corpus } = storeToRefs(stores.useCorpora())
const { canWrite, canDelete } = storeToRefs(stores.useUser())

// Once not falsy, respective modal is shown.
const newCorpus = ref<boolean>()
const deleteCorpus = ref<CorpusMetadata>()
const editCorpus = ref<CorpusMetadata>()

// Deepcopy so we can modify the object freely.
function copy(corpus: CorpusMetadata): CorpusMetadata {
    return structuredClone(toRaw(corpus))
}

// #lifecycle
onMounted(reload)
</script>
