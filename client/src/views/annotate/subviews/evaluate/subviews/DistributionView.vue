<template>
    <GCard>
        <GTable helpLink="evaluation" :columns :loading :items="itemsToDisplay" sortColumn="count">
            <template #title>Distribution</template>
            <template #table-empty>
                <p v-if="distribution">No results for current filter settings.</p>
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
                <GForm v-if="distribution">
                    <fieldset>
                        <label for="lemma-input">Search lemma</label>
                        <GInput if="lemma-input" type="text" v-model="lemmaFilter" placeholder="Lemma" />
                    </fieldset>
                    <fieldset>
                        <label for="literal-input">Search types</label>
                        <GInput if="literal-input" type="text" v-model="literalFilter" placeholder="Type" />
                    </fieldset>
                    <fieldset>
                        <label for="annotation-select">Group by</label>
                        <GSelect
                            id="annotation-select"
                            :options="distributionStore.distributionOptions"
                            v-model="selectedDistribution"
                        />
                    </fieldset>
                    <fieldset>
                        <label for="analysis-select">Single/multiple analyses</label>
                        <GSelect id="analysis-select" :options="singMultiPosOptions" v-model="selectedSingMultiPos" />
                    </fieldset>
                    <fieldset>
                        <label for="pos-select">Include groups</label>
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

            <!-- unique -->
            <template #cell-unique="data">
                <div>{{ `${Object.keys(data.item.tokens).length}` }}</div>
            </template>

            <!-- types -->
            <template #cell-types="data">
                <template v-if="Object.keys(data.item.tokens).length <= 5">
                    <span
                        v-for="(literal, index) in Object.keys(data.item.tokens).sort(function (a, b) {
                            return data.item.tokens[b] - data.item.tokens[a]
                        })"
                        :key="literal"
                    >
                        {{ literal }} <b>{{ `${data.item.tokens[literal]}` }}</b
                        >{{ index != Object.keys(data.item.tokens).length - 1 ? ", " : "" }}
                    </span>
                </template>
                <template v-else>
                    <RightFloatCell>
                        <template #left>
                            <span
                                v-for="literal in Object.keys(data.item.tokens)
                                    .sort(function (a, b) {
                                        return data.item.tokens[b] - data.item.tokens[a]
                                    })
                                    .slice(0, 5)"
                                :key="literal"
                            >
                                {{ literal }}
                                <b>{{ `${data.item.tokens[literal]}` }}</b
                                >,
                            </span>
                            <i
                                >... and
                                {{ Object.keys(data.item.tokens).length - 5 }}
                                more</i
                            >
                        </template>
                        <template #right>
                            <InspectButton @click="typeToken = data.item" />
                        </template>
                    </RightFloatCell>
                </template>
            </template>
        </GTable>

        <TypeTokenModal v-if="typeToken" :typeToken @hide="typeToken = undefined" />
    </GCard>
</template>

<script setup lang="ts">
import stores from "@/stores"
import type { TypeToken } from "@/types/evaluation/distribution"
import type { SelectOption } from "@/types/ui/select"
import MultiSelect from "primevue/multiselect"

// Stores
const distributionStore = stores.useDistribution()
const { distribution, selectedDistribution, loading } = storeToRefs(distributionStore)

// Fields
const selectedPosses = ref<string[]>([])
// Table controls.
const includePos = ref<Record<string, boolean>>({})
const lemmaFilter = ref<string>("")
const literalFilter = ref<string>("")
// GModal for variants
const typeToken = ref<TypeToken>()
// Filtered table items.
const itemsToDisplay = computed((): TypeToken[] => {
    if (!distribution.value) return []
    return distribution.value
            // Case insensitive string comparison.
            .filter((t: TypeToken) => t.lemma.toLowerCase().includes(lemmaFilter.value.toLowerCase()))
            .filter((t: TypeToken) => selectedPosses.value.includes(t.group))
            // Filter by single/multiple PoS
            .filter((t: TypeToken) => {
                if (selectedSingMultiPos.value === "single") return !t.group.includes("+")
                if (selectedSingMultiPos.value === "multiple") return t.group.includes("+")
                return true
            })
            // Case insensitive string comparison.
            // join on \n, as it can't be entered into a <input type=text>
            .filter((t: TypeToken) =>
                Object.keys(t.tokens).join("\n").toLowerCase().includes(literalFilter.value.toLowerCase()),
            )
})
const columns = [
    { key: "lemma" },
    { key: "group" },
    { key: "count", align: "right", sortOn: (t: TypeToken): number => t.count },
    { key: "unique", label: "unique", align: "right", sortOn: (t: TypeToken) => Object.keys(t.tokens).length },
    { key: "types" },
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
