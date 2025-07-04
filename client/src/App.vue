<template>
    <GTabs
        class="level-1"
        basePath=""
        :tabs="[
            { id: 'annotate', title: 'Annotate & Evaluate' },
            { id: 'overview', title: 'Taggers & Datasets' },
            { id: 'user', title: 'User' },
        ]"
    >
        <template #banner>
            <AppBanner />
        </template>
    </GTabs>

    <GModal v-if="errors.length > 0" title="Ocharme!" @hide="reset">
        <p>
            Please try again or contact
            <MailAddress /> for support.
        </p>
        <GInfo v-for="(error, i) in errors" error :key="i">{{ error }}</GInfo>
    </GModal>
</template>

<script setup lang="ts">
import stores from "@/stores"

// #stores
const errorStore = stores.useErrors()
const { reset } = errorStore
const { errors } = storeToRefs(errorStore)

// Load user globally to confirm connection
stores.useUser()
</script>
