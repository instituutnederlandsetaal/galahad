<template>
    <section class="tabs">
        <header class="header">
            <template v-if="$slots.banner">
                <slot name="banner"></slot>
            </template>

            <nav class="nav">
                <slot v-for="tab in tabs" :key="tab.id" :name="`${tab.id}-title`">
                    <a v-if="tab.disabled" class="nav-link disabled">{{ tab.title || tab.id }}</a>
                    <router-link v-else class="nav-link" :to="{ path: `${basePath}/${tab.id}`, query: route.query }">
                        {{ tab.title || tab.id }}
                    </router-link>
                </slot>
            </nav>
        </header>

        <RouterView class="view" />
    </section>
</template>

<script setup lang="ts">
import type { Tab } from "@/types/ui/tab"

const { basePath, tabs } = defineProps<{ basePath: string; tabs: Tab[] }>()
const route = useRoute()
</script>

<style scoped lang="scss">
.tabs {
    display: flex;
    flex-direction: column;
    width: 100%;

    .header {
        font-family: "Schoolboek", "Helvetica Neue", Helvetica, sans-serif;

        .nav {
            display: inline-flex;
            line-height: 45px;
            background-color: var(--int-theme);

            :deep(.nav-link) {
                font-style: normal;
                text-align: center;
                display: block;
                min-width: 80px;
                padding: 0 20px;
                line-height: inherit;
                text-decoration: none;
                color: black;
                user-select: none;

                &:hover:not(.disabled):not(.router-link-active) {
                    cursor: pointer;
                    background-color: var(--int-theme-hover);
                }

                &.disabled {
                    opacity: 0.5;
                    background-color: var(--int-very-light-grey);
                    // Same color as default css button:disabled
                    color: rgb(109, 109, 109);
                    cursor: not-allowed;
                }

                &.router-link-active {
                    background-color: var(--int-theme-outline);
                }

                &:active:not(.disabled):not(.router-link-active) {
                    background-color: var(--int-theme-active);
                }
            }
        }
    }

    .view {
        flex: 1;
    }

    &.level-1 {
        min-height: 100vh;

        > .header .nav {
            min-width: 100%;
        }
    }

    &.level-2 {
        padding: 1rem;

        .view {
            border: var(--int-light-grey) 1px solid;
        }
    }

    &.level-3 {
        flex: 1;
    }
}

@media (max-width: 730px) {
    .tabs {
        &.level-2 {
            padding: 1rem 0;
        }
    }
}
</style>
