<template>
    <GTable class="termCompTable" :items :columns headless>
        <template #cell="data: Cell">
            <div :class="{ incorrect: !itemEqual(data) }">
                {{ data.value }}
            </div>
        </template>
    </GTable>
</template>

<script setup lang="ts">
// Libraries & stores

import stores from "@/stores"
// Types & API
import type { Term } from "@/types/evaluation"
import type { Column } from "@/types/ui/table"

// Stores
const jobSelection = stores.useJobSelection()

// Custom types
type Item = Term & { layer: string }
type Cell = { field: Column; item: Item; value: string }

// Props
const props = defineProps<{
    hypoTerm: Term
    refTerm: Term
}>()

// Fields
const ignorableAnnotations = ["token", "id", "misc"]

// Computed
const items: Ref<Record<string, string>[]> = computed(() => {
    return [
        { layer: jobSelection.hypothesisId, ...props.hypoTerm.annotations },
        { layer: jobSelection.referenceId, ...props.refTerm.annotations }
    ]
})
/** columns are simply all unique keys in term.annotations: Record<string, string> */
const columns: Ref<Column[]> = computed(() => {
    return items.value
        .reduce((acc, item) => {
            Object.keys(item).forEach(key => {
                if (!acc.includes(key)) acc.push(key)
            })
            return acc
        }, [] as string[])
        .filter(i => !ignorableAnnotations.includes(i))
        .map(key => ({ key, label: key }))
})

// Methods
function itemEqual(data: Cell): bool {
    if (
        data.item.layer === jobSelection.referenceId ||
        data.column.key === "layer"
    )
        return true // always true for source layer

    const sourceItem = items.value.find(
        i => i.layer === jobSelection.referenceId
    )
    return annotationsEqual(data.value, sourceItem[data.column.key])
}

function annotationsEqual(refAnnot: string, hypoAnnot: string) {
    return cleanAnnotation(refAnnot) === cleanAnnotation(hypoAnnot)
}

function cleanAnnotation(term) {
    return term?.toLowerCase().replace("_", "")
}
</script>

<style scoped lang="scss">
.incorrect {
    background-color: rgba(255, 0, 0, 0.1);
}

.termCompTable :deep(td) {
    padding: 0 !important;
}
</style>
