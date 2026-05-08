<template>
    <fieldset>
        <label for="format-select">Format</label>
        <GSelect id="format-select" :options title="Select format" v-model="format" />
    </fieldset>
</template>

<script setup lang="ts">
import stores from "@/stores"
const { format } = storeToRefs(stores.useExport())
const { options } = stores.useExport()
</script>
