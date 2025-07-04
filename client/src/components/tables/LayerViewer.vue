<template>
    <GTable :columns :items>
        <template #title>Annotations preview</template>
        <template #help>Here you can see a preview of the annotations.</template>
    </GTable>
</template>

<script setup lang="ts">
import type { DocumentMetadata } from "@/types/documents"
import type { Job } from "@/types/jobs"

// # props
const { document, job } = defineProps<{ document?: DocumentMetadata; job?: Job }>()

// #computed
const annotations = computed(() => (document ? document.annotations : job.tagger.annotations))
const columns = computed(() => annotations.value.map((i) => ({ key: i, label: i })))
const terms = computed(() => (document ? document.layerPreview.terms : job.preview.terms))
const items = computed(() => terms.value.map((t) => t.annotations))
</script>
