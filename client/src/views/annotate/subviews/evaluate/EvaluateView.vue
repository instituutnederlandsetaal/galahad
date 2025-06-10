<template>
    <AnnotateTab>
        <GCard :title="`Evaluate corpus ${corporaStore.corpus?.name}`" helpLink="evaluation">
            <template #help>
                <EvaluateHelp />
            </template>
            <form @submit.prevent class="form">
                <fieldset class="fieldset">
                    <JobSelect />
                </fieldset>
                <fieldset class="fieldset">
                    <JobSelect :isReference="true" />
                </fieldset>
                <fieldset class="fieldset">
                    <label for="csv-download">Download as CSV</label>
                    <i v-if="!jobSelection.hypothesisId || !jobSelection.referenceId">
                        Select both layers first.
                    </i>
                    <DownloadButton v-else id="csv-download" wide :loading="evaluation.loading"
                        @click="evaluation.downloadCSV()" />
                </fieldset>
            </form>
        </GCard>
        <GTabs class="level-3" :basePath :tabs="[
            { id: 'distribution', title: 'Distribution' },
            { id: 'global_metrics', title: 'Global Metrics' },
            { id: 'grouped_metrics', title: 'Grouped Metrics' },
            { id: 'confusion', title: 'Pos Confusion' },
            { id: 'document_layer_comparison', title: 'Document View' },
            { id: 'entities', title: 'Entities' },
        ]">
        </GTabs>
    </AnnotateTab>
</template>

<script setup lang="ts">
import stores from "@/stores"

// Stores
const evaluation = stores.useEvaluation()
const jobSelection = stores.useJobSelection()
const corporaStore = stores.useCorpora()

const { basePath } = defineProps<{
    basePath: string
}>()
</script>

<style scoped lang="scss">
.form {
    display: flex;
    align-items: start;
    flex-wrap: wrap;
    gap: 2rem;

    .fieldset {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 1rem;
        max-width: 500px;
    }
}
</style>
