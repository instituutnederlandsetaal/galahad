<template>
    <InspectButton @click="showModal = true" />
    <GModal v-if="showModal" @hide="showModal = false">
        <template #title><slot name="title"></slot></template>
        <ul>
            <li v-for="item in items" :key="item.name">
                <dl>
                    <dt>
                        <b>{{ item.name }}:</b>
                    </dt>
                    <dd>
                        <ExternalLink :href="item.url">
                            {{ item.description ?? item.url }}
                        </ExternalLink>
                    </dd>
                </dl>
            </li>
        </ul>
    </GModal>
</template>

<script setup lang="ts">
import type { LinkItem } from "@/types/taggers"

const { items } = defineProps<{ items: LinkItem[] }>()
const showModal = ref<boolean>()
</script>

<style scoped lang="scss">
ul {
    padding: 0 1rem;
    li {
        padding: 0.25rem 0;
    }
}
</style>
