<template>
    <div>
        <GTable title="Tagsets overview" :columns :items="tagsetsStore.tagsets">
            <template #help>
                Here you can see an overview of possible tagsets to annotate Dutch.
                <br />
                Currently only TDN-Core is used in the platform.
            </template>

            <template #table-empty-instruction>
                No tagsets appeared? That is not right! Please contact the INT at
                <MailAddress />
            </template>

            <template #cell-punctuationTags="d">
                {{ d.value.join(", ") }}
            </template>
        </GTable>
    </div>
</template>

<script setup lang="ts">
// Libraries & stores
import stores from "@/stores"

// Stores
const tagsetsStore = stores.useTagsets()

// Fields
const columns = [
    { key: "shortName", label: "abbreviation" },
    { key: "longName", label: "name" },
]
</script>
