<template>
    <GTable :title :columns :items :loading helpLink="evaluation" class="metricsTable" :sortColumn>
        <template v-if="$slots.help" #help>
            <slot name="help"></slot>
            <DifferentTagsetsHelp />
        </template>

        <template #table-empty> Select a reference layer and a hypothesis layer to generate metrics. </template>

        <template #header v-if="loading">
            <p>Generating metrics for large corpora may take a while...</p>
        </template>

        <template #header v-else>
            <slot name="header"></slot>
        </template>

        <template
            v-for="cell in [
                'cell-accuracy',
                'cell-precision',
                'cell-recall',
                'cell-f1',
                'cell-macroPrecision',
                'cell-microPrecision',
                'cell-macroRecall',
                'cell-microRecall',
                'cell-macroF1',
                'cell-microF1',
                'cell-microAccuracy',
            ]"
            #[cell]="data"
        >
            <div :key="cell">
                {{ `${data.value ? parseFloat(data.value).toString().slice(0, 4) : 0}` }}
            </div>
        </template>

        <template
            v-for="cell in ['cell-falsePositive', 'cell-falseNegative', 'cell-truePositive', 'cell-noMatch']"
            #[cell]="data"
        >
            <div :key="cell">
                <GButton :disabled="data.value?.count === 0" @click="openModal(data)">
                    {{ data.value?.count }}
                </GButton>
            </div>
        </template>
    </GTable>

    <ComparisonModal
        v-if="samples"
        @hide="samples = undefined"
        :samples
        @download="$emit('download', modalData)"
        :referenceJob="jobSelection.referenceId"
        :hypothesisJob="jobSelection.hypothesisId"
        :downloading
    />
</template>

<script setup lang="ts">
// Libraries & stores
import type { Column, Item, TableData } from "@/types/ui/table"
import stores from "@/stores"
import type { TermComparison, Samples, Metrics } from "@/types/evaluation"

// Stores
const jobSelection = stores.useJobSelection()

// Props
const {
    title = "Metrics",
    columns,
    items,
    loading,
    sortColumn = "count",
    downloading,
} = defineProps<{
    title: string
    columns: Column<Item>[]
    items: Item[]
    loading: boolean
    sortColumn: string
    downloading: boolean
}>()

// Emits
const emit = defineEmits<{ download: [modalData: TableData<Metrics>] }>()

// Fields
const samples = ref<Samples>()
const modalData = ref({})

// Methods
/**
 * Open a set of samples in a modal.
 */
function openModal(data): void {
    modalData.value = data
    samples.value = {
        title: `${data.column.label} ${data.item.name} samples`,
        samples: data.value.samples,
        annotationType: data.item.column.toLowerCase(),
    }
}
</script>

<style scoped lang="scss">
.metricsTable td {
    button {
        display: block;
        text-align: center;
        width: 100%;
        height: 100%;
        margin: 0;
        background-color: transparent;

        &:hover {
            background-color: var(--int-light-grey) !important;
        }

        &:focus {
            background-color: var(--int-light-grey-hover) !important;
        }
    }
}

table button {
    display: block;
    width: initial;
    white-space: initial;
    margin: auto;
}

.metricsTable :deep(td) {
    padding: 0 10px !important;
    margin: 0;
}

.metricsTable :deep(.table-control) {
    min-height: auto;
}
</style>
