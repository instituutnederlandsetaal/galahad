<!-- Do not put a comment between <template> and <AnnotateTab>
    It breaks vue... https://github.com/vuejs/vue/issues/2253 -->

<template>
    <AnnotateTab>
        <!-- title-only card -->
        <GCard :title="`Evaluate corpus ${corporaStore.activeCorpus?.name}`" helpSubject="evaluation">
            <template #help>
                <component :is="help.evaluate"></component>
            </template>
            <!-- Explicitly empty body-->
            <template></template>
        </GCard>
        <!-- Choose hypothesis and reference -->
        <div style="display: flex; width: 100%; align-items: center; justify-content: center; flex-wrap: wrap">
            <JobSelect />
            <JobSelect :isReference="true" />
            <GCard title="Download as CSV">
                <i v-if="!jobSelection.hypothesisJobId || !jobSelection.referenceJobId">Select both layers first.</i>
                <DownloadButton v-else wide @click="evaluation.downloadCSV()" :loading="evaluation.loading" />
            </GCard>
        </div>

        <!-- Table tabs -->
        <GTabs
            ref="tabs"
            class="level-3"
            :basePath="basePath"
            :tabs="[
                { id: 'distribution', title: 'Distribution' },
                { id: 'global_metrics', title: 'Global Metrics' },
                { id: 'grouped_metrics', title: 'Grouped Metrics' },
                { id: 'confusion', title: 'Pos Confusion' },
                { id: 'document_layer_comparison', title: 'Document View' },
            ]">
        </GTabs>
    </AnnotateTab>
</template>

<script setup lang="ts">
// Libraries & stores

import stores, {
    DistributionStore,
    JobsStore,
    MetricsStore,
    EvaluationStore,
    JobSelectionStore,
    ConfusionStore,
    CorporaStore,
    DocumentsStore,
} from "@/stores"

import help from "@/components/help"
import { SOURCE_LAYER } from "@/types/jobs"

// Stores
const jobsStore = stores.useJobs()
const evaluation = stores.useEvaluation()
const confusion = stores.useConfusion()
const distribution = stores.useDistribution()
const metrics = stores.useMetrics()
const jobSelection = stores.useJobSelection()
const corporaStore = stores.useCorpora()
const documentsStore = stores.useDocuments()

// Fields
const props = defineProps(["basePath"])

// Methods
/**
 * Whether the corpusUUID or the hypothesis/reference has changed since the last generated evaluation.
 */
function evaluationRequestHasChanged(): boolean {
    return (
        evaluation.corpusUUID !== corporaStore.activeUUID ||
        evaluation.hypothesis !== jobSelection.hypothesisJobId ||
        evaluation.reference !== jobSelection.referenceJobId
    )
}

/**
 * Reloads the three evaluation types for the current hypothesis and reference.
 * Because distribution is affected solely by hypothesis changes, it is not reloaded by default.
 * Check whether the hypothesis has changed yourself.
 * @param reloadDistribution Whether to reload the distribution as well.
 */
function reloadEvaluationData(reloadDistribution = false) {
    if (!corporaStore.activeCorpus) return
    if (!jobSelection.selectionsValid) return
    const hypothesis = jobSelection.hypothesisJobId
    const reference = jobSelection.referenceJobId

    // Only reload if either the corpus uuid or the hypothesis/reference has changed.
    if (!evaluationRequestHasChanged()) {
        return
    }

    if (reference != null && hypothesis != null) {
        confusion.reloadForUUIDHypothesisReference(
            corporaStore.activeUUID,
            hypothesis,
            reference,
        )
        metrics.reloadForUUIDHypothesisReference(
            corporaStore.activeUUID,
            hypothesis,
            reference,
        )
    }
    // Distribution is unaffected by reference changes, so explicitly ask for it.
    if (reloadDistribution && hypothesis != null) {
        distribution.reloadForUUIDHypothesis(
            corporaStore.activeUUID,
            hypothesis,
        )
    }

    // Save the evaluation request, so we don't reload it again (we cache it).
    if (hypothesis != null) evaluation.hypothesis = hypothesis
    if (reference != null) evaluation.reference = reference
    evaluation.corpusUUID = corporaStore.activeUUID
}

// Watches & mounts
onMounted(() => {
    // Always reset, because e.g. the selected corpus might have changed
    if (!evaluationRequestHasChanged()) {
        return
    }
    confusion.reset()
    metrics.reset()
    distribution.reset()
})

// On corpus (=dataset) selection, reload jobs & docs for that corpus
watch(
    () => corporaStore.activeCorpus,
    () => {
        // Jobs needed for jobs <select>.
        jobsStore.reload()
        // Docs needed to determine whether the sourceLayer job has annotations.
        documentsStore.reloadDocumentsForCorpus(corporaStore.activeUUID)
    },
    { immediate: true },
)

// Reload data on job selection changes.
watch(
    () => jobSelection.hypothesisJobId,
    () => {
        if (!jobSelection.selectionsValid) return
        reloadEvaluationData(true)
    },
)
watch(
    () => jobSelection.referenceJobId,
    () => {
        if (!jobSelection.selectionsValid) return
        reloadEvaluationData()
    },
)
// OnLoad, we also have to wait on validation.
watch(
    () => jobSelection.selectionsValid,
    () => {
        if (jobSelection.selectionsValid) {
            reloadEvaluationData(true)
        }
        // At this point, invalid selections are set to null.
        // So we can override the reference to the sourceLayer as a default (if it exists).
        if (
            jobSelection.referenceJobId == null &&
            documentsStore.numSourceAnnotations > 0
        ) {
            jobSelection.referenceJobId = SOURCE_LAYER
        }
    },
    { immediate: true },
)
</script>
