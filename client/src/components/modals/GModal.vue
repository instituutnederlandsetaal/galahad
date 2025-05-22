<template>
    <!-- v-if instead of v-show such that elements inside a GModal can rely on onMounted()-->
    <dialog v-if="show" ref="modal" class="dialog" @click.self="$emit('hide')" @keyup.esc="$emit('hide')">
        <GCard class="content" :class="{ small: small }" :title>
            <template v-if="$slots.title" #title>
                <slot name="title"></slot>
            </template>
            <template v-if="$slots.help" #help>
                <slot name="help"></slot>
            </template>
            <slot></slot>
        </GCard>
        <div class="buttons">
            <GButton red @click="$emit('hide')">Close</GButton>
            <slot name="buttons"></slot>
        </div>
    </dialog>
</template>

<script setup lang="ts">
const { show, title, small } = defineProps<{
	show: boolean
	title?: string
	small?: boolean
}>()
</script>

<style scoped lang="scss">
.dialog {
    border: 0;
    left: 0;
    top: 0;
    background-color: var(--int-very-light-grey);
    position: fixed;
    height: 100%;
    width: 100%;
    z-index: 2;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    gap: 1rem;
    padding: 1rem;

    .content {
        overflow: auto;
        border: 1px solid var(--int-light-grey);
        width: 100%;
        padding: 2rem;

        &.small {
            width: fit-content;
        }
    }

    .buttons {
        display: flex;
        justify-content: center;
        gap: 1rem;
    }
}

@media (max-width: 800px) or (max-height: 700px) {
    .dialog {
        padding: 0;
        padding-bottom: 0.5em; // Override for bottom button
        gap: 0.5em;
    }
}
</style>
