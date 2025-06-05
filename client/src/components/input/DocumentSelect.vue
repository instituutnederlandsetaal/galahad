<template>
    <label for="document-select">Document:</label>
    <GSelect v-model="model" :options id="document-select" />
</template>

<script setup lang="ts">
import stores from "@/stores"
import type { SelectOption } from "@/types/ui/select"

const documentsStore = stores.useDocuments()

const model = defineModel<string>()

const options = computed<SelectOption[]>(() => {
    return documentsStore.documents.map(doc => ({
        value: doc.name,
        text: doc.name
    }))
})
</script>
