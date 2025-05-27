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
    model: any
    validityDescriptor: string
    validator: (value: any) => boolean
}>()

const isValid = computed(() => (model === undefined ? false : validator(model)))
</script>

<style scoped lang="scss">
.feedback {
    border-bottom: 2px solid var(--int-red);
    margin-left: 10px;
    padding: 2px;

    &.valid {
        border-bottom: 2px solid var(--gold);
    }
}
</style>