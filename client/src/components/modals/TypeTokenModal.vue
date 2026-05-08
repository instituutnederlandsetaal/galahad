<template>
    <GModal @hide="$emit('hide')">
        <GTable :columns :items sortColumn="count">
            <template #title>
                Types of lemma <i>{{ typeToken?.lemma }}</i> and group <i>{{ typeToken?.group }}</i>
            </template>

            <template #help>
                This is an overview of all types belonging to the chosen lemma &mdash; grouped annotation pair.
            </template>
        </GTable>
    </GModal>
</template>

<script setup lang="ts">
import type { TypeToken } from "@/types/evaluation/distribution"
import type { Column } from "@/types/ui/table"

const { typeToken } = defineProps<{ typeToken: TypeToken }>()

const columns: Column<TypeToken>[] = [{ key: "type" }, { key: "count" }]
const items = computed(() => Object.entries(typeToken.tokens).map(([type, count]) => ({ type, count })))
</script>
