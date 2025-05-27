<template>
    <div>
        <GTable class="right" :title="'Distribution of ' + (jobSelection.hypothesisJobId || 'the hypothesis layer')"
            helpLink="evaluation" :columns :items="itemsToDisplay" :loading="distributionStore.loading" displayOnEmpty
            sortedByColumn="count">
            <template #table-empty-instruction>
                <p v-if="distribution.generated">No results for current filter settings.</p>
                <p v-else>Select a hypothesis layer and an annotation to generate a distribution.</p>
            </template>
            <template #help>
                <p>
                    The distribution shows what lemma, part-of-speech pairs have been assigned to which types. When
                    there are more than five types you can click on the inspect symbol to view all types of a lemma-PoS
                    combination.
                </p>
            </template>
            <template #header>
                <p>
                    <b v-if="distribution.trimmed">
                        Because of the large corpus size only the 1000 most frequent lemma, part-of-speech pairs are
                        shown.
                    </b>
                </p>
            </template>

            <!-- count -->
            <template #cell-count="data">
                <div>{{ `${data.value}` }}</div>
            </template>

            <!-- variantCount -->
            <template #cell-variantCount="data">
                <div>{{ `${Object.keys(data.item.literals.literals).length}` }}</div>
            </template>

            <!-- variants-->
            <template #cell-variants="data">
                <div style="min-width: 200px">
                    <template v-if="Object.keys(data.item.literals.literals).length <= 5">
                        <span v-for="(literal, index) in Object.keys(data.item.literals.literals).sort(function (a, b) {
                            return data.item.literals.literals[b] - data.item.literals.literals[a]
                        })" :key="literal">
                            {{ literal }} <b>{{ `${data.item.literals.literals[literal]}` }}</b>{{ index !=
                                Object.keys(data.item.literals.literals).length - 1 ? ", " : "" }}
                        </span>
                    </template>
                    <template v-else>
                        <RightFloatCell>
                            <template #left>
                                <span v-for="literal in Object.keys(data.item.literals.literals)
                                    .sort(function (a, b) {
                                        return data.item.literals.literals[b] - data.item.literals.literals[a]
                                    })
                                    .slice(0, 5)" :key="literal">
                                    {{ literal }}
                                    <b>{{ `${data.item.literals.literals[literal]}` }}</b>,
                                </span>
                                <i>... and
                                    {{ Object.keys(data.item.literals.literals).length - 5 }}
                                    more</i>
                            </template>
                            <template #right>
                                <InspectButton @click="variantsToDisplay = data.item" />
                            </template>
                        </RightFloatCell>
                    </template>
                </div>
            </template>

            <template #prepend>
                <div style="display: flex; justify-content: center" v-if="distributionStore.distributions">
                    <div>
                        <label for="annotation-select">Annotation:</label>
                        <GSelect id="annotation-select" :options="distributionStore.distributionOptions"
                            v-model="selectedDistribution" />
                    </div>
                </div>
                <template v-if="distribution.generated">

                    <div class="table-controls">
                        <!-- search lemma-->
                        <div class="table-control" id="searchLemma">
                            Search lemma:
                            <GInput type="text" v-model="lemmaFilter" placeholder="Lemma" clearBtn />
                        </div>
                        <!-- search literals -->
                        <div class="table-control" id="searchWordForms">
                            Search types:
                            <GInput type="text" v-model="literalFilter" placeholder="Type" clearBtn />
                        </div>
                    </div>
                    <div class="table-controls">
                        <div class="table-control">
                            <label for="analysis-select">single/multiple PoS:</label>
                            <GSelect id="analysis-select" :options="singMultiPosOptions"
                                v-model="selectedSingMultiPos" />
                        </div>
                        <div class="table-control">
                            Include PoS: <br />
                            <MultiSelect v-model="selectedPosses" :options="filteredPosses" placeholder="Select PoS"
                                :maxSelectedLabels="5" />
                        </div>
                    </div>
                </template>
            </template>
        </GTable>

        <VariantsModal :variantsToDisplay :show="variantsToDisplay !== null" @hide="variantsToDisplay = null"
            id="modal" />
    </div>
</template>

<script setup lang="ts">
// Libraries & stores

import stores from "@/stores"

// API & types
import type { Distribution } from "@/types/evaluation"
import type { SelectOption } from "@/types/ui/select"

import MultiSelect from "primevue/multiselect"

// Stores
const distributionStore = stores.useDistribution()
// Doesn't need to be ref'ed, but it's easier to read.
const { distribution, selectedDistribution } = storeToRefs(distributionStore)
const jobSelection = stores.useJobSelection()

// Fields
const selectedPosses = ref([])
// Table controls.
const includePos = ref({} as { [pos: string]: boolean })
const lemmaFilter = ref("")
const literalFilter = ref("")
// GModal for variants
const variantsToDisplay = ref(null as null | Distribution)
// Filtered table items.
const itemsToDisplay = computed((): Distribution[] => {
    // When distribution not yet generated.
    if (!distribution.value?.distribution?.length) return []

    return (
        distribution.value?.distribution
            // Case insensitive string comparison.
            .filter(x =>
                x.lemma.toLowerCase().includes(lemmaFilter.value.toLowerCase()),
            )
            .filter(x => selectedPosses.value.includes(x.pos))
            // Filter by single/multiple PoS
            .filter(x => {
                if (selectedSingMultiPos.value === "single")
                    return !x.pos.includes("+")
                if (selectedSingMultiPos.value === "multiple")
                    return x.pos.includes("+")
                return true
            })
            // Case insensitive string comparison.
            // join on \n, as it can't be entered into a <input type=text>
            .filter(x =>
                Object.keys(x.literals.literals)
                    .join("\n")
                    .toLowerCase()
                    .includes(literalFilter.value.toLowerCase()),
            )
    )
})
const columns = [
    { key: "lemma", label: "lemma", sortOn: (x: Distribution) => x.lemma },
    { key: "pos", label: "PoS", sortOn: (x: Distribution) => x.pos },
    {
        key: "count",
        label: "total\noccurrences",
        sortOn: (x: Distribution) => x.count,
    },
    {
        key: "variantCount",
        label: "number\nof types",
        sortOn: (x: Distribution) => Object.keys(x.literals.literals).length,
    },
    { key: "variants", label: "types" },
]
const singMultiPosOptions: SelectOption[] = [
    { value: "single", text: "Single" },
    { value: "multiple", text: "Multiple" },
    { value: "both", text: "Both" },
]
const selectedSingMultiPos = ref(singMultiPosOptions[0].value)
const filteredPosses = computed(() => {
    if (selectedSingMultiPos.value === "single") {
        return distributionStore.posses.filter(pos => !pos.includes("+"))
    }
    if (selectedSingMultiPos.value === "multiple") {
        return distributionStore.posses.filter(pos => pos.includes("+"))
    }
    return distributionStore.posses
})

// Watches
/**
 * On switching jobs, turn on all PoS checkboxes. We check for change in distributionStore.posses, not in
 * jobSelection.hypothesisJobId, because of the network delay.
 */
watch(
    () => distributionStore.posses,
    () => {
        distributionStore.posses.forEach(pos => (includePos.value[pos] = true))
    },
    { immediate: true },
)

watch(
    () => filteredPosses.value,
    () => {
        selectedPosses.value = filteredPosses.value
    },
    { immediate: true },
)
</script>

<style scoped lang="scss">
.table-controls {
    padding-top: 10px;
}

.table-control {
    min-height: 0px !important;
}

#searchWordForms,
#searchLemma {
    flex: 1;
    max-width: 200px;
}

div:not(#modal)::v-deep() .g-card .content-wrapper .content {
    display: flex;
    flex-direction: column;
    justify-content: safe center;
    align-items: safe center;
}

:deep(table) {
    max-width: 100%;

    th {
        word-break: break-word;
    }
}

.posGrid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
    column-gap: 0px;
}

.posGrid span>div {
    width: fit-content;
}

:deep(#prepend) {
    display: flex;
    flex-direction: column;
    width: 100%;
}
</style>
