<template>
    <GInput v-model="model" list="tagsets" placeholder="Tagset name" />
    <datalist id="tagsets">
        <option v-for="tagset in tagsets" :key="tagset.name" :value="tagset.name"></option>
    </datalist>
</template>

<script setup lang="ts">
import stores from "@/stores"

// #model
const model = defineModel<string>()

// #stores
const { tagsets } = storeToRefs(stores.useTagsets())
</script>
