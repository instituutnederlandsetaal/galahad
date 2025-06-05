<template>
    <GCard title="Tagsets overview">
        <template #help>
            <p>
                Here you can see an overview of possible tagsets to annotate Dutch.
            </p>
            <p>
                <i>Currently only TDN-Core is used in the platform.</i>
            </p>
        </template>

        <GTable :columns :items="tagsets.tagsets" :loading="tagsets.loading">
            <template #table-empty>
                No tagsets appeared? That is not right! Please contact the INT at
                <MailAddress />
            </template>
        </GTable>
    </GCard>
</template>

<script setup lang="ts">
import stores from "@/stores"

const tagsets = stores.useTagsets()
const columns = [{ key: "name" }, { key: "description" }]
</script>
