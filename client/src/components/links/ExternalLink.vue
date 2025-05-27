<template>
    <a :href target="_blank" rel="noopener noreferrer">
        <slot></slot>
    </a>
</template>

<script setup lang="ts">
defineProps<{
    href: string
}>()
</script>
