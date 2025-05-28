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

    <GModal :show="errorsStore.errors.length > 0" title="Ocharme!" @hide="errorsStore.resetErrors">
        <p>
            Please try again or contact
            <MailAddress /> for support.
        </p>
        <GInfo v-for="(error, i) in errorsStore.errors" :key="i" error>{{ error }}</GInfo>
    </GModal>
</template>

<script setup lang="ts">
import stores from "@/stores"

const errorsStore = stores.useErrors()
const userStore = stores.useUser()

onMounted(() => {
    userStore.fetchUser()
})
</script>

<style lang="scss">
#app {
    background-color: var(--int-very-light-grey);
    min-height: 100vh;
}
</style>
