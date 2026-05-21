<template>
    <GCard article :title="`Logged in as ${user.name}`">
        <p>Find information about your account here.</p>
        <dl>
            <dt>Username:</dt>
            <dd>
                <b>{{ user.name }}</b>
            </dd>
            <template v-if="user.admin">
                <dt>Admin:</dt>
                <dd><b>true</b></dd>
            </template>
        </dl>
    </GCard>
</template>

<script setup lang="ts">
import useUser from "@/stores/user"

const { user } = storeToRefs(useUser())
</script>
