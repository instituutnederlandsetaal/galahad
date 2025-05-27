<template>
    <svg class="icon" height="100" width="100" viewBox="0 0 100 100" :class="classes">
        <path class="grail"
            d="M10 10 L90 10 Q90 40 65 40 L65 80 Q80 80 80 90 L20 90 Q20 80 35 80 L35 80 L35 40 Q10 40 10 10 Z" />
        <ellipse class="wine" cx="60" cy="90" rx="30" ry="10" />
    </svg>
</template>

<script setup lang="ts">
const props = defineProps<{
    error?: boolean
    small?: boolean
    still?: boolean
}>()
const classes = {
    error: props.error,
    small: props.small,
    still: props.still,
}
</script>

<style scoped lang="scss">
.icon {
    height: 40px;
    width: 40px;
    animation: spin 4s cubic-bezier(0.68, -0.55, 0.265, 1.55) infinite;
    fill: var(--int-yellow-outline);

    .wine {
        visibility: hidden;
    }

    &.small {
        height: 20px;
        width: 20px;
    }

    &.still {
        animation: none;
    }

    &.error {
        .wine {
            visibility: visible;
            fill: var(--int-red);
        }

        .grail {
            transform-origin: 45% 45%;
            transform: scale(0.6, 0.6) rotate(150deg);
        }
    }
}

@keyframes spin {
    0% {
        transform: rotate(0deg);
    }

    25% {
        transform: rotate(90deg);
    }

    50% {
        transform: rotate(180deg);
    }

    75% {
        transform: rotate(270deg);
    }

    100% {
        transform: rotate(360deg);
    }
}
</style>
