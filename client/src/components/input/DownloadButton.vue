<template>
    <GButton green :loading :disabled title="Download">
        <i class="fa fa-download" :class="{ wide: wide }"></i>
    </GButton>
</template>

<script setup lang="ts">
const { disabled, loading, wide } = defineProps<{
    disabled?: boolean
    loading?: boolean
    wide?: boolean
}>()
</script>

<style scoped lang="scss">
.wide {
    padding: 0 1rem;
}
</style>
