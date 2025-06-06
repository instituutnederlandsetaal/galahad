<template>
    <GTable :columns :items="items">
        <template #title>Annotations of {{ document?.name }}</template>
        <template #help>Here you can inspect a small part of the source layer of the document.</template>
    </GTable>
</template>

<script setup lang="ts">
import type { DocumentMetadata } from "@/types/documents"
import type { Job } from "@/types/jobs"

const { document, job } = defineProps<{
    document?: DocumentMetadata
    job?: Job
}>()

const annotations = computed(() =>
    document ? document.annotations : job.tagger.annotations
)
const columns = computed(() =>
    annotations.value.map(i => ({ key: i, label: i }))
)
const terms = computed(() =>
    document ? document.layerPreview.terms : job.preview.terms
)
const items = computed(() => terms.value.map(t => t.annotations))
</script>
