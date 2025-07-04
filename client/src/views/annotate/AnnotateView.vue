<template>
    <GTabs
        class="level-2"
        basePath="/annotate"
        :tabs="[
            { id: 'corpora', title: 'Corpora' },
            { id: 'documents', title: 'Documents', disabled: !corpus },
            { id: 'jobs', title: 'Jobs', disabled: !corpus?.numDocs },
            { id: 'evaluate', title: 'Evaluate', disabled: !corpus?.numDocs },
            { id: 'export', title: 'Export', disabled: !corpus?.numDocs },
        ]"
    />
</template>

<script setup lang="ts">
import stores from "@/stores"

// #stores
const { corpus } = storeToRefs(stores.useCorpora())
</script>
