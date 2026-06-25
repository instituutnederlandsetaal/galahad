<template>
    <GTable :title :columns :items :loading helpLink="evaluation" class="metricsTable" :sortColumn>
        <template v-if="$slots.help" #help>
            <slot name="help"></slot>
            <DifferentTagsetsHelp />
        </template>

        <template #empty> Select a reference layer and a hypothesis layer to generate metrics. </template>

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
            <div :key="cell" style="text-align: right">
                {{ `${data.value ? parseFloat(data.value).toString().slice(0, 5) : 0}` }}
            </div>
        </template>

        <template v-for="cell in ['cell-falsePositive', 'cell-truePositive']" #[cell]="d: TableData<any>" :key="cell">
            <GButton :disabled="d.value?.count === 0" @click="model = d" style="text-align: right" plain>
                {{ `${((d.value.count / d.item.hypCount) * 100).toFixed(1)}%` }}
                <i>({{ d.value.count.toString() }})</i>
            </GButton>
        </template>

        <template v-for="cell in ['cell-falseNegative']" #[cell]="d: TableData<any>" :key="cell">
            <GButton :disabled="d.value?.count === 0" @click="model = d" style="text-align: right" plain>
                {{ `${((d.value.count / d.item.refCount) * 100).toFixed(1)}%` }}
                <i>({{ d.value.count.toString() }})</i>
            </GButton>
        </template>

        <template v-for="cell in ['cell-noMatch']" #[cell]="d: TableData<any>" :key="cell">
            <GButton :disabled="d.value?.count === 0" @click="model = d" style="text-align: right" plain>
                {{ d.value.count.toString() }}
            </GButton>
        </template>
    </GTable>
</template>

<script setup lang="ts">
// Libraries & stores
import type { Column, Item, TableData } from "@/types/ui/table"
import type { TermComparison, Samples, Metrics } from "@/types/evaluation"
import useLayers from "@/stores/layers"

const model = defineModel()

// Stores
const { hypothesisId, referenceId, hypothesisLayer, referenceLayer } = storeToRefs(useLayers())

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
const tableData = ref<TableData<any>>()

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
