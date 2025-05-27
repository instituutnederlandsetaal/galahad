<template>
    <code :class="classes"><slot></slot></code>
</template>

<script setup lang="ts">
const { green, orange, red } = defineProps<{
    green?: boolean;
    orange?: boolean;
    red?: boolean;
}>()
const classes = {
    green: green,
    orange: orange,
    red: red,
}
</script>

<style scoped lang="scss">
code {
    font-size: 0.8rem;
    padding: 0.2rem 0.3rem;

    &.green {
        background-color: var(--int-green);
    }

    &.orange {
        background-color: var(--int-orange);
    }

    &.red {
        background-color: var(--int-red);
    }
}
</style>
