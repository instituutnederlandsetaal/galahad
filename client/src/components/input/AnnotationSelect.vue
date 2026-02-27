<template>
    <fieldset>
        <label for="annotation-select">Annotation</label>
        <GSelect id="annotation-select" :options v-model="model" />
    </fieldset>
</template>

<script setup lang="ts">
import type { SelectOption } from "@/types/ui/select"

// #model
const model = defineModel<string>()

// #props
const { options } = defineProps<{ options: SelectOption[] }>()
</script>
