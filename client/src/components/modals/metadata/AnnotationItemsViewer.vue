<template>
    <div>
        <span>
            {{ items.map((a) => a.annotation).join(", ") }}
        </span>
        <InspectButton @click="showModal = true" />
        <GModal v-if="showModal" @hide="showModal = false">
            <template #title><slot name="title"></slot></template>
            <ul>
                <li v-for="item in items" :key="item.annotation">
                    {{ item.annotation }}
                    <dl>
                        <template v-for="principle in item.principles" :key="principle.name">
                            <dt>
                                <b>{{ principle.name }}:</b>
                            </dt>
                            <dd>
                                <ExternalLink :href="principle.href">
                                    {{ principle.details ?? principle.href }}
                                </ExternalLink>
                            </dd>
                        </template>
                    </dl>
                </li>
            </ul>
        </GModal>
    </div>
</template>

<script setup lang="ts">
import type { AnnotationItem } from "@/types/taggers"

const { items } = defineProps<{ items: AnnotationItem[] }>()
const showModal = ref<boolean>()
</script>

<style scoped lang="scss">
div {
    display: flex;
    gap: 0.5rem;
    align-items: center;
    justify-content: end;
    ul {
        padding: 0 1rem;
        li {
            padding: 0.25rem 0;
            dl {
                padding: 0 1rem;
            }
        }
    }
}
</style>
