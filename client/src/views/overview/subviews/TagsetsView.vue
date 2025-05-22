<template>
    <GCard title="Tagsets overview">
        <template #help>
            Here you can see an overview of possible tagsets to annotate Dutch.
            <br />
            Currently only TDN-Core is used in the platform.
        </template>

        <GTable :columns :items="tagsetsStore.tagsets" :loading="tagsetsStore.loading">
            <template #table-empty-instruction>
                No tagsets appeared? That is not right! Please contact the INT at
                <MailAddress />
            </template>
        </GTable>
    </GCard>
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
