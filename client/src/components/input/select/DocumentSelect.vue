<template>
    <fieldset>
        <label for="document-select">Document</label>
        <GSelect v-model="model" :options id="document-select" />
    </fieldset>
</template>

<script setup lang="ts">
import useDocuments from "@/stores/documents"
import type { SelectOption } from "@/types/ui/select"

const { documents } = storeToRefs(useDocuments())

const model = defineModel<string>()

const options = computed<SelectOption[]>(() => {
    return documents.value.map((doc) => ({ value: doc.name, text: doc.name }))
})
</script>
