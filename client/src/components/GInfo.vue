<template>
    <aside class="info" :class="{ error: error }">
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
const { error, spinner } = defineProps<{ error?: boolean; spinner?: boolean }>()
</script>

<style scoped lang="scss">
.info {
    padding: 1rem 2rem 1rem 4rem;
    border: 1px solid var(--int-light-grey);
    max-width: 800px;
    position: relative;
    min-height: 70px;
    width: fit-content;

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
