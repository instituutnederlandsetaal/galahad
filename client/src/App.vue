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

    <GModal :show="appStore.errors.length > 0" title="Ocharme!" small @hide="appStore.resetErrors">
        <p>Please try again or contact
            <MailAddress /> for support.
        </p>
        <GInfo v-for="(error, i) in appStore.errors" :key="i" error>{{ error }}</GInfo>
    </GModal>
</template>

<script setup lang="ts">
// Stores
import stores from "@/stores"

const appStore = stores.useApp()
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

// @media (max-width: 800px) or (max-height: 700px) {
//     #app {
//         overflow: auto;
//     }

//     .tabs.level-1 {
//         height: initial !important;
//         min-height: 100% !important;

//         & > .header {
//             position: initial;
//         }
//     }
// }


//     .tabs > .content {
//         padding: 10px 0 0 0 !important;
//     }
// }
</style>
