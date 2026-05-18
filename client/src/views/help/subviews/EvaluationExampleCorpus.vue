<template>
    <GTable :columns :items />
</template>

<script setup lang="ts">
import type { Column } from "@/types/ui/table"

// #types
type Example = {
    token: string
    aPos: string // Actual Part of Speech
    pPos: string // Predicted Part of Speech
}

// #data
const columns: Column<Example>[] = [
    { key: "token", label: "Token", noSort: true },
    { key: "aPos", label: "Actual PoS", noSort: true },
    { key: "pPos", label: "Predicted PoS", noSort: true },
]
const items: Example[] = [
    { token: "lopen (to run)", aPos: "verb", pPos: "noun" },
    { token: "snel (fast)", aPos: "adjective", pPos: "adjective" },
    { token: "hond (dog)", aPos: "noun", pPos: "noun" },
    { token: "mooie (beautiful)", aPos: "adjective", pPos: "adjective" },
    { token: "spring (jump)", aPos: "verb", pPos: "verb" },
    { token: "katten (cats)", aPos: "noun", pPos: "noun" },
    { token: "lui (lazy)", aPos: "adjective", pPos: "noun" },
    { token: "vos (fox)", aPos: "noun", pPos: "noun" },
    { token: "speelde (played)", aPos: "verb", pPos: "verb" },
    { token: "blij (happy)", aPos: "adjective", pPos: "adjective" },
]
</script>
