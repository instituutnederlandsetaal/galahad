<template>
    <span class="feedback" :class="{ valid: isValid }">
        <i class="fa" :class="{ 'fa-times': !isValid, 'fa-check': isValid }"></i>
        <i>
            {{ validityDescriptor }}
        </i>
    </span>
</template>

<script setup lang="ts">
const { model, validityDescriptor, validator } = defineProps<{
    model: unknown
    validityDescriptor: string
    validator: (value: unknown) => boolean
}>()

const isValid = computed<boolean>(() => validator(model))
</script>

<style scoped lang="scss">
.feedback {
    display: inline-block;
    border-bottom: 2px solid var(--int-red);
    margin-left: 10px;
    padding: 2px;

    &.valid {
        border-bottom: 2px solid var(--gold);
    }
}
</style>
