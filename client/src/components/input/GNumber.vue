<template>
    <input v-model.number="model" type="number" :min :max :step :placeholder />
    <GValidator v-if="validator && validityDescriptor" :model :validator :validityDescriptor />
</template>

<script setup lang="ts">
const model = defineModel<number>()

const { placeholder, min, max, step, validityDescriptor, validator } = defineProps<{
    placeholder?: string
    min?: number
    max?: number
    step?: number
    validityDescriptor?: string
    validator?: (value: unknown) => boolean
}>()
</script>

<style scoped lang="scss">
input[type="number"] {
    font: inherit;
    height: 39px;
    font-size: 1rem;
    padding-left: 5px;
    border: 1px solid #ccc;
    width: 209px;
    background-color: white;

    &:focus {
        outline: var(--int-theme-active) solid 2px;
    }
}
</style>
