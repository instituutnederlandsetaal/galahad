<template>
    <GTabs class="level-1" basePath="" :tabs="[
        { id: 'annotate', title: 'Annotate & Evaluate' },
        { id: 'overview', title: 'Taggers & Datasets' },
        { id: 'user', title: 'User' },
    ]">
        <template #banner>
            <AppBanner />
        </template>
    </GTabs>

    <GModal v-if="errors.errors.length > 0" title="Ocharme!" @hide="errors.reset">
        <p>
            Please try again or contact
            <MailAddress /> for support.
        </p>
        <GInfo v-for="(error, i) in errors.errors" :key="i" error>{{ error }}</GInfo>
    </GModal>
</template>

<script setup lang="ts">
import stores from "@/stores"

const errors = stores.useErrors()
// Load user globally to confirm connection
stores.useUser()
</script>

<style lang="scss">
#app {
    background-color: var(--int-very-light-grey);
    min-height: 100vh;
}
</style>
