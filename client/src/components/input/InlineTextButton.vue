<template>
    <code :class="classes"><slot></slot></code>
</template>

<script setup lang="ts">
const { green, orange, red, plain } = defineProps<{
    green?: boolean
    orange?: boolean
    red?: boolean
    plain?: boolean
}>()
const classes = {
    green: green,
    orange: orange,
    red: red,
    plain: plain,
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

    &.plain {
        border: 1px solid var(--int-light-grey);
    }
}
</style>
