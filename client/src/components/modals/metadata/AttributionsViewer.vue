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
                        <ExternalLink :href="item.href">
                            {{ item.details ?? item.href }}
                        </ExternalLink>
                    </dd>
                </dl>
            </li>
            <!-- always add version last -->
            <li>
                <dl>
                    <dt>
                        <b>Version:</b>
                    </dt>
                    <dd>
                        {{ version }}
                    </dd>
                </dl>
            </li>
        </ul>
    </GModal>
</template>

<script setup lang="ts">
import type { LinkItem } from "@/types/taggers"

const { items, version } = defineProps<{ items: LinkItem[]; version: string }>()
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
