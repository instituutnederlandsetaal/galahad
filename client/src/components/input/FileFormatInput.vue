<template>
    <label for="format-select">Format:</label>
    <GSelect title="Select a format" id="format-select" v-model="exportStore.format" :options />
</template>

<script setup lang="ts">
import stores from "@/stores"
import { Format } from "@/types/documents"
import type { SelectOption } from "@/types/ui/select"

const exportStore = stores.useExport()
const userStore = stores.useUser()

const options: SelectOption[] = [
    { value: Format.CONLLU, text: "CoNLL-U (Universal Dependencies)" },
    { value: Format.FOLIA, text: "FoLiA (Format for Linguistic Annotation)" },
    { value: Format.NAF, text: "NAF (NLP Annotation Format) " },
    { value: Format.TEI_P5, text: "TEI P5 (Text Encoding Initiative)" },
    { value: Format.TSV, text: "TSV (Tab-separated values)" },
]
// Admins can also export txt.
if (userStore.user.isAdmin) {
    options.push({ value: Format.TXT, text: "TXT (Plain text)" })
}
</script>
