<template>
    <a :href target="_blank" rel="noopener noreferrer">
        <slot></slot>
    </a>
</template>

<script setup lang="ts">
const { href } = defineProps<{ href: string }>()
</script>
