<template>
    <div>
        <GInput type="select" :options="options" v-model="exportStore.format" />
    </div>
</template>

<script setup lang="ts">
// Libraries & stores
import stores from "@/stores"
// API & types
import { Format } from "@/types/documents"

// Stores
const exportStore = stores.useExport()
const userStore = stores.useUser()

// Fields
const options = [
    { value: Format.Conllu, text: "CoNLL-U (Universal Dependencies)" },
    { value: Format.Folia, text: "FoLiA (Format for Linguistic Annotation)" },
    { value: Format.Naf, text: "NAF (NLP Annotation Format) " },
    { value: Format.Tei_p5, text: "TEI P5 (Text Encoding Initiative)" },
    { value: Format.Tsv, text: "TSV (Tab-separated values)" },
]
// Admins can also export txt.
if (userStore.user.isAdmin) {
    // This exports the plain text, not the annotations.
    options.push({ value: Format.Txt, text: "TXT (Plain text)" })
}
</script>
