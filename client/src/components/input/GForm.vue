<template>
    <form @submit.prevent :style="{ gap, flexDirection: vertical ? 'column' : 'row' }">
        <slot></slot>
    </form>
</template>

<script setup lang="ts">
// #props
const { gap = "1rem", vertical = false } = defineProps<{ gap?: string; vertical?: boolean }>()
</script>

<style scoped lang="scss">
form {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    justify-content: center;

    > :deep(*) {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 0.5rem;
    }
}
</style>
