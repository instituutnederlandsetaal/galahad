<template>
    <form class="paginator" @submit.prevent>
        <GButton plain :disabled="page == 1" title="First" @click="page = 1">
            1
        </GButton>

        <GButton plain :disabled="page == 1" title="Previous" @click="page -= 1">
            <i class="fa fa-arrow-left"></i>
        </GButton>

        <select v-model="page" class="page-select">
            <option v-for="n in numPages" :key="n" :value="n">
                {{ n }}
            </option>
        </select>

        <GButton plain :disabled="page == numPages" title="Next" @click="page += 1">
            <i class="fa fa-arrow-right"></i>
        </GButton>

        <GButton plain :disabled="page == numPages" title="Last" @click="page = numPages">
            {{ numPages }}
        </GButton>
    </form>
</template>

<script setup lang="ts">
const page = defineModel<number>({ default: 1 })

const { numPages } = defineProps<{
    numPages: number
}>()
</script>

<style scoped lang="scss">
.paginator {
    display: flex;
    min-width: 0;
    gap: 0.5rem;
    justify-content: center;

    button,
    select {
        padding: 0.5rem;
        font-size: 1rem;
        flex: 1 0 36px;
    }

    .page-select {
        border: 1px solid var(--int-grey);
        background: white;

        &:hover {
            background: var(--int-very-light-grey);
        }

        &:active {
            background: var(--int-very-light-grey-hover);
        }

    }
}
</style>
