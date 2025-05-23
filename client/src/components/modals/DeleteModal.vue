<template>
    <GModal small title="Are you sure?" :show @hide="$emit('hide')">
        <p>
            You will <slot name="action">delete</slot> <b>{{ displayname }}</b>. This can not be undone.
        </p>
        <template #buttons>
            <GButton green @click="$emit('delete'); $emit('hide')">Confirm</GButton>
        </template>
    </GModal>
</template>

<script setup lang="ts">
const { displayname, show } = defineProps<{
    displayname: string
    show: boolean
}>()

const emit = defineEmits<{
    delete: []
    hide: []
}>()
</script>
