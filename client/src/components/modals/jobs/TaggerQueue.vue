<template>
    <p v-if="loading">Calculating current server load...</p>
    <p v-else>GaLAHaD is currently processing {{ queue }} {{ queue == 1 ? "job" : "jobs" }}.</p>
</template>

<script setup lang="ts">
import * as API from "@/api/taggers"

const queue = ref<number>(0)
const loading = ref<boolean>(true)

useTimeoutPoll(
    async () => {
        API.getQueue()
            .then((res) => {
                queue.value = res.data
            })
            .finally(() => {
                loading.value = false
            })
    },
    1000,
    { immediate: true, immediateCallback: true },
)
</script>
