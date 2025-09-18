<template>
    <GCard>
        <GTable helpLink="evaluation" :columns :loading :items="itemsToDisplay" sortColumn="count" title="Distribution">
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
                        <GSelect id="annotation-select" :options="distributionOptions" v-model="selectedDistribution" />
                    </fieldset>
                    <fieldset>
                        <label for="analysis-select">Single/multiple analyses</label>
                        <GSelect
                            id="analysis-select"
                            :options="singleMultipleOptions"
                            v-model="selectedSingleMultiple"
                        />
                    </fieldset>
                    <fieldset>
                        <label for="pos-select">Include groups</label>
                        <MultiSelect
                            id="pos-select"
                            v-model="selectedPosses"
                            :options="filteredGroups"
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
const { hypothesisId } = storeToRefs(stores.useJobSelection())
const { distributions, loading } = storeToRefs(stores.useDistribution())
const { reload } = stores.useDistribution()

// Fields
// Selected distribution.
const distributionOptions = computed<SelectOption[]>(() =>
    Object.keys(distributions.value ?? {}).map((x) => ({ value: x, text: x })),
)
const selectedDistribution = ref<string>()
const distribution = computed<TypeToken[]>(() => distributions.value?.[selectedDistribution.value])

const selectedPosses = ref<string[]>([])
// Table controls.
const lemmaFilter = ref<string>("")
const literalFilter = ref<string>("")
// GModal for variants
const typeToken = ref<TypeToken>()

// Filtered table items.
const itemsToDisplay = computed((): TypeToken[] => {
    if (!distribution.value) return []
    return (
        distribution.value
            // // Case insensitive string comparison.
            .filter((t: TypeToken) => t.lemma.toLowerCase().includes(lemmaFilter.value.toLowerCase()))
            // // join on \n, as it can't be entered into a <input type=text>
            .filter((t: TypeToken) =>
                Object.keys(t.tokens).join("\n").toLowerCase().includes(literalFilter.value.toLowerCase()),
            )
            .filter((t: TypeToken) => selectedPosses.value.includes(t.group))
    )
})

const columns = [
    { key: "lemma" },
    { key: "group" },
    { key: "count", align: "right" },
    { key: "unique", label: "unique", align: "right", sortOn: (t: TypeToken) => Object.keys(t.tokens).length },
    { key: "types", noSort: true },
]
const singleMultipleOptions: SelectOption[] = [
    { value: "single", text: "Single" },
    { value: "multiple", text: "Multiple" },
    { value: "both", text: "Both" },
]
const selectedSingleMultiple = ref<string>(singleMultipleOptions[0].value)
const filteredGroups = computed(() => {
    if (selectedSingleMultiple.value === "single") {
        return groups.value.filter((pos) => !pos.includes("+"))
    }
    if (selectedSingleMultiple.value === "multiple") {
        return groups.value.filter((pos) => pos.includes("+"))
    }
    return groups.value
})
const groups = computed<string[]>(() =>
    [...new Set(Object.values(distribution.value || {}).map((t: TypeToken) => t.group))].sort(),
)

// Watches
watch(hypothesisId, reload, { immediate: true })

watch(distributionOptions, () => (selectedDistribution.value = distributionOptions.value[0]?.value))

watch(filteredGroups, () => (selectedPosses.value = filteredGroups.value))
</script>
