<template>
    <a :href="`mailto:${address}`">
        {{ address }}
    </a>
</template>

<script setup lang="ts">
const { address = "servicedesk@ivdnt.org" } = defineProps<{ address?: string }>()
</script>
