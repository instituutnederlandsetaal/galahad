<template>
    <div class="layerpreview">
        <GTable v-if="layerNotEmpty" compact :columns :items noHelp title="Preview" />
        <p v-else><i>No layer found</i></p>
    </div>
</template>

<script setup lang="ts">
// Libraries & stores

// API & types
import { LayerPreview } from "@/types/jobs"
// Component dependencies.

const props = defineProps({ layer: Object as () => LayerPreview })
const layerNotEmpty = computed(() => {
    return props.layer?.terms?.length > 0
})

const columns = computed(() => {
    const annotations = Object.keys(props.layer?.terms[0].annotations).filter((i) => i != "token")
    const columns = annotations.map((annotation) => ({ key: annotation, label: annotation }))
    // Token always as the first column
    columns.unshift({ key: "token", label: "Token" })
    return columns
})

const items = computed(() => {
    return props.layer?.terms.map((term) => {
        return term.annotations
    })
})
</script>

<style scoped lang="scss">
:deep(h3) {
    margin: 0;
}

p {
    text-align: center;
}
</style>
