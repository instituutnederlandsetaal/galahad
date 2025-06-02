<template>
    <!-- v-if instead of v-show such that elements inside a GModal can rely on onMounted()-->
    <dialog v-if="show" ref="modal" class="modal view" tabindex="0" @click.self="$emit('hide')"
        @keyup.esc="$emit('hide')">
        <GCard class="content" :title>
            <template v-if="$slots.title" #title>
                <slot name="title"></slot>
            </template>
            <template v-if="$slots.help" #help>
                <slot name="help"></slot>
            </template>
            <slot></slot>
        </GCard>
        <form class="buttons" @submit.prevent>
            <GButton red @click="$emit('hide')">Close</GButton>
            <slot name="buttons"></slot>
        </form>
    </dialog>
</template>

<script setup lang="ts">
const { show, title } = defineProps<{
    show: boolean
    title?: string
}>()

const emit = defineEmits<{
    hide: []
}>()

onMounted(() => {
    addEventListener("keyup", event => {
        if (event.key === "Escape") {
            emit("hide")
        }
    })
})
</script>

<style scoped lang="scss">
.modal {
    border: 0;
    left: 0;
    top: 0;
    background-color: rgba(255, 255, 255, 0.8); // var(--int-very-light-grey) with 0.8 alpha
    backdrop-filter: blur(5px);

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
        border: 1px solid var(--int-light-grey);
        width: fit-content;
    }

    .buttons {
        display: flex;
        justify-content: center;
        gap: 1rem;
    }
}

@media (max-width: 800px) or (max-height: 700px) {
    .modal {
        padding: 0;
        padding-bottom: 0.5rem; // Override for bottom button
        gap: 0.5rem;
    }
}
</style>
