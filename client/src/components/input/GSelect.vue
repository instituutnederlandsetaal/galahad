<template>
    <select v-model="model" class="select">
        <option class="option" selected :value="undefined">-- select an option --</option>
        <option v-for="option in options" :key="option.value" :value="option.value" class="option">
            {{ option.text }}
        </option>
    </select>
</template>

<script setup lang="ts">
import type { SelectOption } from "@/types/ui/select"

const model = defineModel<string>()

const { options } = defineProps<{ options: SelectOption[] }>()
</script>

<style scoped lang="scss">
.select {
    border: 0px solid black;
    height: 34px;
    max-width: 300px;
    -webkit-appearance: none;
    -moz-appearance: none;
    appearance: none;
    -moz-border-radius: 0px;
    -webkit-border-radius: 0px;
    border-radius: 0px;
    padding: 0.375rem 1.75rem 0.375rem 0.75rem;
    outline-width: 0;
    // Background-color is taken from the firefox default.
    background: rgb(233, 233, 237)
        url("data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='4' height='5'%3E%3Cpath fill='%23343a40' d='M2 0L0 2h4zm0 5L0 3h4z'/%3E%3C/svg%3E")
        no-repeat right 0.75rem center/8px 10px;
    font-size: 1rem;
    cursor: pointer;

    &:hover {
        background-color: var(--int-very-light-grey-hover);
    }

    &:active {
        background-color: var(--int-light-grey);
    }
}
</style>
