<template>
    <GTable class="termCompTable" :items :columns headless>
        <template #cell="data: Cell">
            <div :class="{ incorrect: !itemEqual(data) }" style="padding: 0.5rem">
                {{ data.value }}
            </div>
        </template>
    </GTable>
</template>

<script setup lang="ts">
// Libraries & stores

import stores from "@/stores"
// Types & API
import { Term } from "@/types/evaluation"
import { Field } from "@/types/table"

// Stores
const jobSelection = stores.useJobSelection()

// Custom types
type Item = Term & { layer: string }
type Cell = { field: Field; item: Item; value: string }

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
		{ layer: jobSelection.hypothesisJobId, ...props.hypoTerm.annotations },
		{ layer: jobSelection.referenceJobId, ...props.refTerm.annotations },
	]
})
/** columns are simply all unique keys in term.annotations: Record<string, string> */
const columns: Ref<Field[]> = computed(() => {
	return items.value
		.reduce((acc, item) => {
			Object.keys(item).forEach((key) => {
				if (!acc.includes(key)) acc.push(key)
			})
			return acc
		}, [] as string[])
		.filter((i) => !ignorableAnnotations.includes(i))
		.map((key) => ({ key, label: key }))
})

// Methods
function itemEqual(data: Cell): bool {
	if (
		data.item.layer == jobSelection.referenceJobId ||
		data.field.key == "layer"
	)
		return true // always true for source layer

	const sourceItem = items.value.find(
		(i) => i.layer == jobSelection.referenceJobId,
	)
	return annotationsEqual(data.value, sourceItem[data.field.key])
}

function annotationsEqual(refAnnot: string, hypoAnnot: string) {
	return cleanAnnotation(refAnnot) == cleanAnnotation(hypoAnnot)
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
