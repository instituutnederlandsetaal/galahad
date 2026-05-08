<template>
    <AnnotateTab>
        <GCard :title="`Evaluate corpus ${corpus.name}`" helpLink="evaluation">
            <template #help>
                <EvaluateHelp />
            </template>
            <GForm>
                <JobSelect />
                <JobSelect :isReference="true" />
                <fieldset>
                    <label for="csv-download">Download as CSV</label>
                    <i v-if="!hypothesisId || !referenceId"> Select both layers first. </i>
                    <DownloadButton v-else id="csv-download" wide :loading @click="downloadCSV" />
                </fieldset>
            </GForm>
        </GCard>
        <GTabs
            class="level-3"
            basePath="/annotate/evaluate"
            :tabs="[
                { id: 'distribution', title: 'Distribution' },
                { id: 'global_metrics', title: 'Global Metrics' },
                { id: 'grouped_metrics', title: 'Grouped Metrics' },
                { id: 'confusion', title: 'Confusion' },
            ]"
        >
        </GTabs>
    </AnnotateTab>
</template>

<script setup lang="ts">
import stores from "@/stores"

// #stores
const evaluationStore = stores.useEvaluation()
const { downloadCSV } = evaluationStore
const { loading } = storeToRefs(evaluationStore)
const { hypothesisId, referenceId } = stores.useJobSelection()
const { corpus } = storeToRefs(stores.useCorpora())
</script>
