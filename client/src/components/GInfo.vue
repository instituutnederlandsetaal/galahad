<template>
    <aside class="info" :class="classes">
        <GSpinner class="spinner" :error :still="!spinner" />
        <slot>Someone forgot to put the info.</slot>
        <template v-if="$slots.footer">
            <footer class="footer">
                <slot name="footer"></slot>
            </footer>
        </template>
    </aside>
</template>

<script setup lang="ts">
// library
import { computed } from "vue"
// props
const props = defineProps<{
    error?: boolean
    spinner?: boolean
}>()
// computed
const classes = computed(() => ({
    error: props.error,
}))
</script>

<style scoped lang="scss">
.info {
    padding: 1rem 2rem 1rem 4rem;
    border: 1px solid var(--int-light-grey);
    margin: 10px auto;
    max-width: 1200px;
    position: relative;
    min-height: 70px;

    &.error {
        border: 2px solid var(--int-red);
    }

    .spinner {
        position: absolute;
        left: 0.8rem;
        top: 0.8rem;
    }

    :deep(h2) {
        margin-top: 0;
    }

    .footer {
        padding-top: 0.5rem;
        text-align: center;
    }
}
</style>
