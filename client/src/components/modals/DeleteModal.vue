<template>
    <GModal title="Are you sure?" @hide="$emit('hide')">
        <p>
            You will <slot name="action">delete</slot> <b>{{ itemName }}</b>. This can not be undone.
        </p>
        <template #buttons>
            <GButton green @click="$emit('delete'); $emit('hide')">Confirm</GButton>
        </template>
    </GModal>
</template>

<script setup lang="ts">
const { itemName } = defineProps<{
    itemName: string
}>()

const emit = defineEmits<{
    delete: []
    hide: []
}>()
</script>
