<template>
    <GCard>
        <GTable
            helpLink="evaluation"
            :columns
            :items="itemsToDisplay"
            :loading="distributionStore.loading"
            displayOnEmpty
            sortColumn="count"
        >
            <template #title>Distribution of {{ jobSelection.hypothesisId }}</template>
            <template #table-empty>
                <p v-if="distribution.generated">No results for current filter settings.</p>
                <p v-else>Select a hypothesis layer and an annotation to view a distribution.</p>
            </template>
            <template #help>
                <p>
                    The distribution shows what lemma, part-of-speech pairs have been assigned to which types. When
                    there are more than five types you can click on the inspect symbol to view all types of a lemma-PoS
                    combination.
                </p>
            </template>

            <template #header>
                <p v-if="distribution.trimmed">
                    <i>
                        Because of the large corpus size only the 1000 most frequent lemma, part-of-speech pairs are
                        shown.
                    </i>
                </p>

                <GForm>
                    <fieldset>
                        <label for="lemma-input">Search lemma</label>
                        <GInput if="lemma-input" type="text" v-model="lemmaFilter" placeholder="Lemma" />
                    </fieldset>
                    <fieldset>
                        <label for="literal-input">Search types</label>
                        <GInput if="literal-input" type="text" v-model="literalFilter" placeholder="Type" />
                    </fieldset>
                    <fieldset>
                        <label for="annotation-select">Annotation</label>
                        <GSelect
                            id="annotation-select"
                            :options="distributionStore.distributionOptions"
                            v-model="selectedDistribution"
                        />
                    </fieldset>
                    <fieldset>
                        <label for="analysis-select">single/multiple analyses</label>
                        <GSelect id="analysis-select" :options="singMultiPosOptions" v-model="selectedSingMultiPos" />
                    </fieldset>
                    <fieldset>
                        <label for="pos-select">Include values</label>
                        <MultiSelect
                            id="pos-select"
                            v-model="selectedPosses"
                            :options="filteredPosses"
                            placeholder="Select PoS"
                            :maxSelectedLabels="5"
                        />
                    </fieldset>
                </GForm>
            </template>

            <!-- variantCount -->
            <template #cell-variantCount="data">
                <div>{{ `${Object.keys(data.item.literals.literals).length}` }}</div>
            </template>

            <!-- variants-->
            <template #cell-variants="data">
                <template v-if="Object.keys(data.item.literals.literals).length <= 5">
                    <span
                        v-for="(literal, index) in Object.keys(data.item.literals.literals).sort(function (a, b) {
                            return data.item.literals.literals[b] - data.item.literals.literals[a]
                        })"
                        :key="literal"
                    >
                        {{ literal }} <b>{{ `${data.item.literals.literals[literal]}` }}</b
                        >{{ index != Object.keys(data.item.literals.literals).length - 1 ? ", " : "" }}
                    </span>
                </template>
                <template v-else>
                    <RightFloatCell>
                        <template #left>
                            <span
                                v-for="literal in Object.keys(data.item.literals.literals)
                                    .sort(function (a, b) {
                                        return data.item.literals.literals[b] - data.item.literals.literals[a]
                                    })
                                    .slice(0, 5)"
                                :key="literal"
                            >
                                {{ literal }}
                                <b>{{ `${data.item.literals.literals[literal]}` }}</b
                                >,
                            </span>
                            <i
                                >... and
                                {{ Object.keys(data.item.literals.literals).length - 5 }}
                                more</i
                            >
                        </template>
                        <template #right>
                            <InspectButton @click="variantsToDisplay = data.item" />
                        </template>
                    </RightFloatCell>
                </template>
            </template>
        </GTable>

        <VariantsModal :variantsToDisplay v-if="variantsToDisplay" @hide="variantsToDisplay = undefined" />
    </GCard>
</template>

<script setup lang="ts">
import stores from "@/stores"
import type { Distribution } from "@/types/evaluation"
import type { SelectOption } from "@/types/ui/select"
import MultiSelect from "primevue/multiselect"

// Stores
const distributionStore = stores.useDistribution()
// Doesn't need to be ref'ed, but it's easier to read.
const { distribution, selectedDistribution } = storeToRefs(distributionStore)
const jobSelection = stores.useJobSelection()

// Fields
const selectedPosses = ref<string[]>([])
// Table controls.
const includePos = ref<Record<string, boolean>>({})
const lemmaFilter = ref<string>("")
const literalFilter = ref<string>("")
// GModal for variants
const variantsToDisplay = ref<Distribution>()
// Filtered table items.
const itemsToDisplay = computed((): Distribution[] => {
    // When distribution not yet generated.
    if (!distribution.value?.distribution?.length) return []

    return (
        distribution.value?.distribution
            // Case insensitive string comparison.
            .filter((x) => x.lemma.toLowerCase().includes(lemmaFilter.value.toLowerCase()))
            .filter((x) => selectedPosses.value.includes(x.pos))
            // Filter by single/multiple PoS
            .filter((x) => {
                if (selectedSingMultiPos.value === "single") return !x.pos.includes("+")
                if (selectedSingMultiPos.value === "multiple") return x.pos.includes("+")
                return true
            })
            // Case insensitive string comparison.
            // join on \n, as it can't be entered into a <input type=text>
            .filter((x) =>
                Object.keys(x.literals.literals).join("\n").toLowerCase().includes(literalFilter.value.toLowerCase()),
            )
    )
})
const columns = [
    { key: "lemma", label: "lemma", sortOn: (x: Distribution) => x.lemma },
    { key: "pos", label: "PoS", sortOn: (x: Distribution) => x.pos },
    { key: "count", label: "count", align: "right", sortOn: (x: Distribution): number => x.count },
    {
        key: "variantCount",
        label: "unique",
        align: "right",
        sortOn: (x: Distribution) => Object.keys(x.literals.literals).length,
    },
    { key: "variants", label: "types" },
]
const singMultiPosOptions: SelectOption[] = [
    { value: "single", text: "Single" },
    { value: "multiple", text: "Multiple" },
    { value: "both", text: "Both" },
]
const selectedSingMultiPos = ref<string>(singMultiPosOptions[0].value)
const filteredPosses = computed(() => {
    if (selectedSingMultiPos.value === "single") {
        return distributionStore.posses.filter((pos) => !pos.includes("+"))
    }
    if (selectedSingMultiPos.value === "multiple") {
        return distributionStore.posses.filter((pos) => pos.includes("+"))
    }
    return distributionStore.posses
})

// Watches
/**
 * On switching jobs, turn on all PoS checkboxes. We check for change in distributionStore.posses, not in
 * jobSelection.hypothesisId, because of the network delay.
 */
watch(
    () => distributionStore.posses,
    () => {
        distributionStore.posses.forEach((pos) => (includePos.value[pos] = true))
    },
    { immediate: true },
)

watch(
    () => distributionStore.distributionOptions,
    () => {
        // Reset selectedDistribution when options change.
        if (distributionStore.distributionOptions.length > 0) {
            selectedDistribution.value = distributionStore.distributionOptions[0].value
        }
    },
)

watch(
    () => filteredPosses.value,
    () => {
        selectedPosses.value = filteredPosses.value
    },
    { immediate: true },
)
</script>
