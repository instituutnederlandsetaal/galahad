<template>
    <GCard :title="`Logged in as ${userStore.user.id}`">
        <template #help>Find information about your account here</template>
        <p>
            Find information about your account here.
            <br /><br />
            Username:<br />
            <b>{{ userStore.user.id }}</b>
            <br /><br />
            <b v-if="userStore.user.isAdmin"> You are an admin.</b>
        </p>
    </GCard>
</template>

<script setup lang="ts">
// Libraries & stores
import stores from "@/stores"

// Stores
const userStore = stores.useUser()
</script>

<style scoped lang="scss">
.center {
    text-align: center;
}

.content {
    background-color: white !important;
}

.g-card {
    overflow-y: auto;
}
</style>
