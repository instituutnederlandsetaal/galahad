<template>
    <GCard :title="`Logged in as ${user.user.id}`">
        <template #help>Find information about your account here</template>
        <p>
            Find information about your account here.
            <br /><br />
            Username:<br />
            <b>{{ user.user.id }}</b>
            <br /><br />
            <b v-if="user.user.isAdmin"> You are an admin.</b>
        </p>
    </GCard>
</template>

<script setup lang="ts">
// Libraries & stores
import stores from "@/stores"

// Stores
const user = stores.useUser()
</script>

<style scoped="true" lang="scss">
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
