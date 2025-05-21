<template>
    <GModal small :show @hide="$emit('hide')">
        <template #title>
            Types of lemma <i>{{ variantsToDisplay?.lemma }}</i> and part-of-speech
            <i>{{ variantsToDisplay?.pos }}</i>
        </template>

        <template #help>
            This is an overview of all types belonging to the chosen lemma, part-of-speech pair.
        </template>

        <GTable compact :columns :items sortedByColumn="occurrences" :sortDesc="true" />
    </GModal>
</template>

<script setup lang="ts">
// Libraries & stores

// Types & API
import { Field } from "@/types/table"
import { Distribution } from "@/types/evaluation"

// Custom types
type DistEntry = { variant: string; occurrences: number }

// Props
const props = defineProps<{
    show: boolean
    variantsToDisplay: Distribution
}>()

// Fields
const columns: Field[] = [
    { key: "variant", label: "Type", sortOn: (x: DistEntry) => x.variant },
    { key: "occurrences", label: "Occurrences", sortOn: (x: DistEntry) => x.occurrences },
]
const items: DistEntry[] = computed(() => {
    return Object.entries(props.variantsToDisplay.literals.literals).map(([variant, occurrences]) => ({
        variant,
        occurrences,
    }))
})
</script>
<style scoped lang="scss">
:deep(.my-small) {
    padding: 1em;
}
</style>
