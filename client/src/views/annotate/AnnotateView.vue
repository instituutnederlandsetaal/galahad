<template>
    <GTabs
        class="level-2"
        basePath="/annotate"
        :tabs="[
            { id: 'corpora', title: 'Corpora' },
            { id: 'documents', title: 'Documents', disabled: !corpus },
            { id: 'jobs', title: 'Jobs', disabled: !corpus?.documents },
            { id: 'evaluate', title: 'Evaluate', disabled: !corpus?.documents },
            { id: 'export', title: 'Export', disabled: !corpus?.documents },
        ]"
    />
</template>

<script setup lang="ts">
import useCorpora from "@/stores/corpora"

const { corpus } = storeToRefs(useCorpora())
</script>
