<template>
    <button :disabled="disabled || loading" :class="classes" @click="$emit('click')">
        <GSpinner small v-if="loading" />
        <slot></slot>
    </button>
</template>

<script setup lang="ts">
const { disabled, red, orange, green, plain, loading } = defineProps<{
    disabled?: boolean
    red?: boolean
    orange?: boolean
    green?: boolean
    plain?: boolean
    loading?: boolean
}>()

const classes = computed<Record<string, boolean>>(() => ({
    red: red,
    orange: orange,
    green: green,
    plain: plain,
    disabled: disabled,
}))

const emit = defineEmits<{ click: [] }>()
</script>

<style scoped lang="scss">
button {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    background-color: var(--int-theme);
    font-size: inherit;
    font-family: inherit;
    padding: 10px;
    font-size: 1rem;
    border: none;
    word-break: keep-all;
    white-space: nowrap;
    width: max-content;
    cursor: pointer;
    line-height: 1.2rem;
    gap: 5px;

    // Align holy grail.
    svg {
        vertical-align: bottom;
    }

    &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
    }
}

// Button colors.
button {
    // Default
    background-color: var(--int-theme);

    &.orange {
        background-color: var(--int-orange);
    }

    &.green {
        background-color: var(--int-green);
    }

    &.red {
        background-color: var(--int-red);
    }

    &.plain {
        background-color: transparent;
        border: 1px solid var(--int-grey);
    }

    // Only use hover & active when not disabled.
    &:not(:disabled) {
        &:hover {
            background-color: var(--int-theme-hover);

            &.orange {
                background-color: var(--int-orange-hover);
            }

            &.green {
                background-color: var(--int-green-hover);
            }

            &.red {
                background-color: var(--int-red-hover);
            }

            &.plain {
                background-color: var(--int-very-light-grey);
            }
        }

        &:active {
            background-color: var(--int-theme-active);

            &.orange {
                background-color: var(--int-orange-active);
            }

            &.green {
                background-color: var(--int-green-active);
            }

            &.red {
                background-color: var(--int-red-active);
            }

            &.plain {
                background-color: var(--int-very-light-grey-hover);
            }
        }
    }
}
</style>
