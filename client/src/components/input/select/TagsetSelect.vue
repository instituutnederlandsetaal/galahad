<template>
    <GInput v-model="model" list="tagsets" placeholder="Tagset name" />
    <datalist id="tagsets">
        <option v-for="tagset in tagsets" :key="tagset.name" :value="tagset.name"></option>
    </datalist>
</template>

<script setup lang="ts">
import useTagsets from "@/stores/static/tagsets"

const model = defineModel<string>()
const { tagsets } = storeToRefs(useTagsets())
</script>
