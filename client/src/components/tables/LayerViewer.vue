<template>
    <GTable :columns :items>
        <template #title>Annotations preview of {{ name }}</template>
        <template #header>
            <dl>
                <dl v-for="[key, value] in Object.entries(summary.annotations)" :key="key">
                    <dt>{{ key }}:</dt>
                    <dd>{{ value }}</dd>
                </dl>
            </dl>
        </template>
    </GTable>
</template>

<script setup lang="ts">
import type { DocumentMetadata } from "@/types/documents"
import type { Job } from "@/types/jobs"

// # props
const { document, job } = defineProps<{ document?: DocumentMetadata; job?: Job }>()

// #computed
const name = computed(() => (document ? document.name : job?.tagger.id))
const summary = computed(() => (document ? document.summary : job.summary))
const annotations = computed(() => Object.keys(summary.value.annotations))
const columns = computed(() => annotations.value.map((i) => ({ key: i, label: i })))
const terms = computed(() => (document ? document.preview.terms : job.preview.terms))
const items = computed(() => terms.value.map((t) => t.annotations))
</script>

<style scoped lang="scss">
dl {
    display: flex;
    gap: 1rem;
    > dl {
        display: inline-flex;
        gap: 0.25rem;
        > dt {
            font-weight: bold;
        }
    }
}
</style>
