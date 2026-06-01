<template>
    <GSelect :options v-model="model" />
</template>

<script setup lang="ts">
import type { LayerMetadata } from "@/types/layers"
import type { SelectOption } from "@/types/ui/select"

const model = defineModel<string>()
const { layer } = defineProps<{ layer?: LayerMetadata }>()
const options = computed<SelectOption[]>(() =>
    Object.keys(layer?.annotations ?? {})
        .filter((s: string) => s != "token")
        .map((s) => ({ value: s, text: s })),
)
</script>
