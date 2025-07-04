<template>
    <label for="annotation-select">Annotation</label>
    <GSelect id="annotation-select" :options v-model="model" />
</template>

<script setup lang="ts">
import type { SelectOption } from "@/types/ui/select"

// #model
const model = defineModel<string>()

// #props
const { options } = defineProps<{ options: SelectOption[] }>()
</script>
