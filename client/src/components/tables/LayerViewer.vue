<template>
    <GTable :columns :items>
        <template #title>Annotations preview of {{ name }}</template>
        <template #header>
            <AnnotationSummary :annotations />
        </template>
    </GTable>
</template>

<script setup lang="ts">
import type { DocumentMetadata } from "@/types/documents"
import type { Job } from "@/types/jobs"

// # props
const { document, job } = defineProps<{ document?: DocumentMetadata; job?: Job }>()

// #computed
const name = computed(() => (document ? document.name : job?.tagger.name))
const annotations = computed(() => (document ? document.annotations : job.annotations))
const columns = computed(() => Object.keys(annotations.value).map((i) => ({ key: i, label: i, noSort: true })))
const terms = computed(() => (document ? document.preview.terms : job.preview.terms))
const items = computed(() => terms.value.map((t) => t.annotations))
</script>
