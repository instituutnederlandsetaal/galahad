<template>
    <GModal title="Are you sure?" @hide="$emit('hide')">
        <p>
            You will <slot name="action">delete</slot> <b>{{ itemName }}</b
            >. This can not be undone.
        </p>
        <template #buttons>
            <GButton
                green
                @click="
                    () => {
                        $emit('delete')
                        $emit('hide')
                    }
                "
                >Confirm</GButton
            >
        </template>
    </GModal>
</template>

<script setup lang="ts">
// #props
const { itemName } = defineProps<{ itemName: string }>()

// #emits
const _emit = defineEmits<{ delete: []; hide: [] }>()
</script>
