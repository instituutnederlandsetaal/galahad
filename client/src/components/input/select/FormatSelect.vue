<template>
    <fieldset>
        <label for="format-select">Format</label>
        <GSelect id="format-select" :options title="Select format" v-model="format" />
    </fieldset>
</template>

<script setup lang="ts">
import useExport from "@/stores/export"
const { format } = storeToRefs(useExport())
const { options } = useExport()
</script>
