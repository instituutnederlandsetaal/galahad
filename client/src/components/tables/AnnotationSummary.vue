<template>
    <dl>
        <dl v-for="[key, value] in Object.entries(annotations ?? {})" :key="key">
            <dt>{{ key }}:</dt>
            <dd>{{ value }}</dd>
        </dl>
    </dl>
</template>

<script setup lang="ts">
import type { AnnotationsSummary } from "@/types/documents"

const { annotations } = defineProps<{ annotations?: AnnotationsSummary }>()
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
