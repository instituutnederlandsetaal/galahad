<template>
    <GCard article :title="`Logged in as ${userStore.user.id}`">
        <p>
            Find information about your account here.
        </p>
        <dl>
            <dt>Username:</dt>
            <dd><b>{{ userStore.user.id }}</b></dd>
        </dl>
        <p v-if="userStore.user.isAdmin">
            <b>You are an admin.</b>
        </p>
    </GCard>
</template>

<script setup lang="ts">
import stores from "@/stores"

const userStore = stores.useUser()
</script>
