<template>
    <GModal @hide="$emit('hide')">
        <template #title>
            Types of lemma <i>{{ variantsToDisplay?.lemma }}</i> and part-of-speech
            <i>{{ variantsToDisplay?.pos }}</i>
        </template>

        <template #help>
            This is an overview of all types belonging to the chosen lemma, part-of-speech pair.
        </template>

        <GTable :columns :items sortColumn="occurrences" :sortDesc="true" />
    </GModal>
</template>

<script setup lang="ts">
import type { Distribution } from "@/types/evaluation"
import type { Column } from "@/types/ui/table"

// Custom types
type DistEntry = { variant: string; occurrences: number }

// Props
const props = defineProps<{
    show: boolean
    variantsToDisplay: Distribution
}>()

// Fields
const columns: Column[] = [
    { key: "variant", label: "Type", sortOn: (x: DistEntry) => x.variant },
    {
        key: "occurrences",
        label: "count",
        sortOn: (x: DistEntry) => x.occurrences
    }
]
const items: DistEntry[] = computed(() => {
    return Object.entries(props.variantsToDisplay.literals.literals).map(
        ([variant, occurrences]) => ({
            variant,
            occurrences
        })
    )
})
</script>
