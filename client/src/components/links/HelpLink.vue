<template>
    <ExternalLink :href="'/galahad/help/general#' + subject">
        read more on <i>{{ subject }}</i>
    </ExternalLink>
</template>

<script setup lang="ts">
const { subject } = defineProps<{
    subject: string
}>()
</script>
