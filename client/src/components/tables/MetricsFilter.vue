<template>
    <div class="table-controls">
        <div class="table-control">
            Annotation:
            <GInput type="select" :options="metricOptions" v-model="selectedMetric" />
        </div>

        <div class="table-control">
            Group by:
            <GInput type="select" :options="groupOptions" v-model="selectedGroup" />
        </div>

        <div class="table-control" v-if="selectedMetric == selectedGroup">
            Single/multiple analysis:
            <GInput type="select" :options="singleOrMultipleOptions" v-model="selectedSingleOrMultiple" />
        </div>
    </div>
</template>

<script setup lang="ts">
// Libraries & stores

import stores from "@/stores"

const { metrics } = storeToRefs(stores.useMetrics())
const props = defineProps(["annotations"])

// Fields
const metricOptions = computed(() => {
    if (props.annotations == null) return []
    const names = Object.keys(props.annotations)
        .filter(key => !(key.startsWith("single") || key.startsWith("multi")))
        .map(key => key.split("By")[0].toLowerCase())
    const uniqueNames = [...new Set(names)]
    return uniqueNames.map(name => ({ value: name, text: name }))
})
const groupOptions = computed(() => {
    if (props.annotations == null) return []
    const names = Object.keys(props.annotations)
        .filter(key => !(key.startsWith("single") || key.startsWith("multi")))
        .map(key => key.split("By")[1].toLowerCase())
    const uniqueNames = [...new Set(names)]
    return uniqueNames.map(name => ({ value: name, text: name }))
})

const singleOrMultipleOptions = [
    { value: "both", text: "Both" },
    { value: "single", text: "Single" },
    { value: "multi", text: "Multiple" },
]
const selectedMetric = ref(metricOptions.value[0]?.value)
const selectedGroup = ref(groupOptions.value[0]?.value)
const selectedSingleOrMultiple = ref(singleOrMultipleOptions[0]?.value)

const metricName = computed(() => {
    let annotation = null
    if (
        selectedSingleOrMultiple.value === "both" ||
        selectedMetric.value !== selectedGroup.value
    ) {
        annotation = selectedMetric.value
    } else {
        annotation =
            selectedSingleOrMultiple.value + capitalize(selectedMetric.value)
    }
    const group = capitalize(selectedGroup.value)
    return `${annotation}By${group}`
})

// Methods
function capitalize(str: string): string {
    return str.charAt(0).toUpperCase() + str.slice(1)
}

defineExpose({
    metricName,
})
</script>
