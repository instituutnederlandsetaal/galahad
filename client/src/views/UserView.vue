<template>
    <GCard article :title="`Logged in as ${user.id}`">
        <p>
            Find information about your account here.
        </p>
        <dl>
            <dt>Username:</dt>
            <dd><b>{{ user.id }}</b></dd>
            <template v-if="user.isAdmin">
                <dt>Admin:</dt>
                <dd><b>true</b></dd>
            </template>
        </dl>
    </GCard>
</template>

<script setup lang="ts">
import stores from "@/stores"

const { user } = storeToRefs(stores.useUser())
</script>
