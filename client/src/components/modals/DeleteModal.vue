<template>
    <GModal small title="Are you sure?" :show @hide="$emit('hide')">
        <template #help>
            {{ displayname }} will be deleted from the GaLAHaD server.
            <br />
            Make sure you have a copy of the original files somewhere.
        </template>
        <p>
            You will <slot name="action">delete</slot> <b>{{ displayname }}</b>. This can not be undone.
        </p>
        <template #buttons>
            <GButton green @click="$emit('delete', item); $emit('hide')">Confirm</GButton>
        </template>
    </GModal>
</template>

<script setup lang="ts">
const { item, displayname, show } = defineProps<{
	item: string
	displayname: string
	show: boolean
}>()

defineEmits<{
	delete: [item: string]
	hide: []
}>()
</script>
