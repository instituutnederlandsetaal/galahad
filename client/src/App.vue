<template>
    <GTabs :tabs class="level-1" basePath="">
        <template #banner>
            <AppBanner />
        </template>
    </GTabs>

    <GFooter />

    <GModal v-if="errors.length > 0" title="Ocharme!" @hide="errors = []">
        <p>
            Please try again or contact
            <MailAddress /> for support.
        </p>
        <GInfo v-for="(error, i) in errors" error :key="i">{{ error }}</GInfo>
    </GModal>
</template>

<script setup lang="ts">
import type { Tab } from "@/types/ui/tab"
import stores from "@/stores"

const { errors } = storeToRefs(stores.useErrors())
const tabs: Tab[] = [
    { id: "annotate", title: "Annotate & Evaluate" },
    { id: "overview", title: "Taggers & Datasets" },
    { id: "user", title: "User" },
]

// Load user globally to confirm connection
stores.useUser()
</script>
