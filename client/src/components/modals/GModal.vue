<template>
    <transition name="fade" mode="out-in">
        <!-- v-if instead of v-show such that elements inside a GModal can rely on onMounted()-->
        <dialog v-if="show" class="dialog" @click.self="$emit('hide')" @keyup.esc="$emit('hide')">
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
    </transition>
</template>

<script setup lang="ts">
// --- props ---
const {
    small = false,
    show,
    title,
} = defineProps<{
    small?: boolean
    show: boolean
    title?: string
}>()

// --- emits ---
defineEmits<{
    hide: []
}>()
</script>

<style scoped lang="scss">
.dialog {
    left: 0;
    top: 0;
    background-color: var(--int-very-light-grey);
    position: fixed;
    height: 100%;
    width: 100%;
    z-index: 2;
    display: flex;
    flex-direction: column;
    box-sizing: border-box;
    justify-content: center;
    align-items: center;
    padding: 2em;
    padding-bottom: 1em; // Override for bottom button
    gap: 1em;
}

.content {
    overflow: auto;
    border: 1px solid var(--int-light-grey);
    box-sizing: border-box;
    width: 100%;
    padding: 2em;
}

.content.small {
    width: fit-content;
}

.content > * {
    margin: 0px; // override default margin
}

.buttons {
    display: flex;
    justify-content: center;
    box-sizing: border-box;
    gap: 1em;
}

@media (max-width: 800px) or (max-height: 700px) {
    .dialog {
        padding: 0;
        padding-bottom: 0.5em; // Override for bottom button
        gap: 0.5em;
    }
}
</style>
