<template>
    <aside class="info" :class="classes">
        <GSpinner class="spinner" :error :still="!spinner" />
        <slot></slot>
        <template v-if="$slots.footer">
            <footer class="footer">
                <slot name="footer"></slot>
            </footer>
        </template>
    </aside>
</template>

<script setup lang="ts">
const props = defineProps<{
	error?: boolean
	spinner?: boolean
}>()

const classes = computed(() => ({
	error: props.error,
}))
</script>

<style scoped lang="scss">
.info {
    padding: 1rem 2rem 1rem 4rem;
    border: 1px solid var(--int-light-grey);
    max-width: 1200px;
    position: relative;
    min-height: 70px;

    display: flex;
    flex-direction: column;
    gap: 1rem;

    &.error {
        border: 2px solid var(--int-red);
    }

    .spinner {
        position: absolute;
        left: 0.8rem;
        top: 0.8rem;
    }

    .footer {
        text-align: center;
    }
}
</style>
