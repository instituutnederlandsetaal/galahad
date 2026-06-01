<template>
    <GCard title="Distribution">
        <template #help>
            <p>
                The distribution shows what lemma, part-of-speech pairs have been assigned to which types. When there
                are more than five types you can click on the inspect symbol to view all types of a lemma-PoS
                combination.
            </p>
        </template>

        <GForm v-if="hypothesisAnnotations.length">
            <fieldset>
                <label for="annotation-select">Annotation</label>
                <AnnotationSelect id="annotation-select" :options="annotationOptions" v-model="selectedAnnotation" />
            </fieldset>
            <fieldset v-if="distribution">
                <label for="lemma-input">Search {{ selectedAnnotation }}</label>
                <GInput id="lemma-input" type="text" v-model="annotationFilter" :placeholder="selectedAnnotation" />
            </fieldset>
            <fieldset v-if="distribution">
                <label for="literal-input">Search types</label>
                <GInput id="literal-input" type="text" v-model="typeFilter" placeholder="Type" />
            </fieldset>
            <fieldset>
                <label for="group-select">Group by</label>
                <AnnotationSelect id="group-select" :options="groupOptions" v-model="selectedGroup" />
            </fieldset>
            <fieldset v-if="distribution">
                <label for="analysis-select">Single/multiple analyses</label>
                <GSelect id="analysis-select" :options="analysesOptions" v-model="selectedAnalysis" />
            </fieldset>
            <fieldset v-if="distribution">
                <label for="groups-select">Select {{ selectedGroup }}</label>
                <MultiSelect
                    id="groups-select"
                    :options="groupsOptions"
                    v-model="selectedGroups"
                    :placeholder="selectedGroup"
                    :maxSelectedLabels="5"
                />
            </fieldset>
        </GForm>
        <p v-else>First, select a hypothesis that has annotations other than <i>token</i>.</p>

        <GTable v-if="distribution" :columns :loading :items sortColumn="count">
            <template #empty>
                <p>No results for current filter settings.</p>
            </template>

            <template #header> </template>

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

        <TypeTokenModal
            v-if="typeToken"
            :typeToken
            :annotation="selectedAnnotation"
            :group="selectedGroup"
            @hide="typeToken = undefined"
        />
    </GCard>
</template>

<script setup lang="ts">
import useDistribution from "@/stores/evaluation/distribution"
import useLayers from "@/stores/layers"
import type { TypeToken } from "@/types/evaluation/distribution"
import type { SelectOption } from "@/types/ui/select"
import MultiSelect from "primevue/multiselect"

// Stores
const { hypothesisAnnotations } = storeToRefs(useLayers())
const { distribution, loading, annotation: selectedAnnotation, group: selectedGroup } = storeToRefs(useDistribution())

// Form
const annotationFilter = ref<string>("")
const typeFilter = ref<string>("")
const analysesOptions: SelectOption[] = [
    { value: "single", text: "Single" },
    { value: "multiple", text: "Multiple" },
    { value: "both", text: "Both" },
]
const selectedAnalysis = ref<string>(analysesOptions[0].value)
const groups = computed<string[]>(() =>
    [...new Set(Object.values(distribution.value ?? {}).map((t: TypeToken) => t.group))].sort(),
)
const groupsOptions = computed(() => {
    if (selectedAnalysis.value === "single") {
        return groups.value.filter((pos) => !pos.includes("+"))
    }
    if (selectedAnalysis.value === "multiple") {
        return groups.value.filter((pos) => pos.includes("+"))
    }
    return groups.value
})
const selectedGroups = ref<string[]>([])

// only logical groups
const annotationOptions = computed(() =>
    hypothesisAnnotations.value.filter((option: SelectOption) => !["head"].includes(option.text)),
)
const groupOptions = computed(() =>
    hypothesisAnnotations.value.filter((option: SelectOption) => !["lemma", "head"].includes(option.text)),
)

watch(
    hypothesisAnnotations,
    () => {
        selectedAnnotation.value = hypothesisAnnotations.value[0]?.value
        selectedGroup.value = hypothesisAnnotations.value[1]?.value
    },
    { immediate: true },
)

watch(
    groupsOptions,
    () => {
        selectedGroups.value = groupsOptions.value
    },
    { immediate: true },
)

// Fields
// Selected distribution.
// const distributionOptions = computed<SelectOption[]>(() =>
//     Object.keys(distributions.value ?? {}).map((x) => ({ value: x, text: x })),
// )
// const selectedDistribution = ref<string>()
// const distribution = computed<TypeToken[]>(() => distributions.value?.[selectedDistribution.value])

// Table controls.
// GModal for variants
const typeToken = ref<TypeToken>()

// Filtered table items.
const items = computed((): TypeToken[] => {
    if (!distribution.value) return []
    return (
        distribution.value
            // // Case insensitive string comparison.
            .filter((t: TypeToken) => t.annotation.toLowerCase().includes(annotationFilter.value.toLowerCase()))
            // // join on \n, as it can't be entered into a <input type=text>
            .filter((t: TypeToken) =>
                Object.keys(t.tokens).join("\n").toLowerCase().includes(typeFilter.value.toLowerCase()),
            )
            .filter((t: TypeToken) => selectedGroups.value.includes(t.group))
    )
})

const columns = computed(() => [
    { key: "annotation", label: selectedAnnotation.value },
    { key: "group", label: selectedGroup.value },
    { key: "count", align: "right" },
    { key: "unique", label: "unique", align: "right", sortOn: (t: TypeToken) => Object.keys(t.tokens).length },
    { key: "types", noSort: true },
])
</script>
