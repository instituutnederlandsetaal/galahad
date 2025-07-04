<template>
    <fieldset>
        <label for="format-select">Format</label>
        <GSelect id="format-select" :options title="Select format" v-model="format" />
    </fieldset>
</template>

<script setup lang="ts">
import stores from "@/stores"
import { Format } from "@/types/documents"
import type { SelectOption } from "@/types/ui/select"

const { format } = storeToRefs(stores.useExport())
const { user } = storeToRefs(stores.useUser())

const options: SelectOption[] = [
    { value: Format.CONLLU, text: "CoNLL-U (Universal Dependencies)" },
    { value: Format.FOLIA, text: "FoLiA (Format for Linguistic Annotation)" },
    { value: Format.NAF, text: "NAF (NLP Annotation Format) " },
    { value: Format.TEI_P5, text: "TEI P5 (Text Encoding Initiative)" },
    { value: Format.TSV, text: "TSV (Tab-separated values)" },
]
// Admins can also export txt.
if (user.isAdmin) {
    options.push({ value: Format.TXT, text: "TXT (Plain text)" })
}
</script>
