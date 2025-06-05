<template>
    <section class="tabs">

        <header class="header">
            <template v-if="$slots.banner">
                <slot name="banner"></slot>
            </template>
            <nav class="nav">
                <template v-for="tab in tabs" :key="tab.id">
                    <a v-if="!tab.disabled && !tab.stub" :href="urlForTab(tab.id)"
                        :class="'textcolor ' + navLinkClass(tab.id)" @click.prevent="navigateTo(tab.id)">
                        <slot :name="`${tab.id}-title`" :isActive="currentTab == tab.id">{{ tab.title || tab.id }}
                        </slot>
                    </a>
                    <span :class="`nav-link ${tab.disabled ? 'disabled' : ''}`" v-else>
                        <slot :name="`${tab.id}-title`">{{ tab.title || tab.id }}</slot>
                    </span>
                </template>
            </nav>
        </header>

        <RouterView class="view" @navigate="router.push" />
    </section>
</template>

<script setup lang="ts">
export type Tab = {
    id: string
    title: string
    disabled?: boolean
    stub?: boolean
}

const { basePath, tabs } = defineProps<{
    basePath: string
    tabs: Tab[]
}>()

const currentTab = ref<string>()
const route = useRoute()
const router = useRouter()

onMounted(() => {
    if (route.path.split("/").length > basePath.split("/").length) {
        // respect the url
        induceCurrentTab()
        return
    }
    const state = localStorage.getItem(`galahad:${basePath}`)
    if (state !== null && state !== undefined) {
        // load state from local storage
        navigateTo(state, true)
    } else {
        // default
        navigateTo(tabs[0].id, true)
    }
})

watch(
    () => route,
    () => {
        induceCurrentTab()
    }
)

// --- methods ---
function induceCurrentTab() {
    const last = route.path.split("/").pop() as string
    setCurrentTab(last)
}
function navigateTo(tabId: string, replace = false) {
    const path = `${basePath}/${tabId}`
    if (!route.path.startsWith(path)) {
        if (replace) {
            router.replace({ path: path, query: route.query })
        } else {
            router.push({ path: path, query: route.query })
        }
    }
    setCurrentTab(tabId)
}
function navLinkClass(tabId: string) {
    return `nav-link${currentTab.value === tabId ? " active" : ""}`
}
function setCurrentTab(tabId: string) {
    // Since the route is not reactive, we have to update the value like this
    currentTab.value = tabId
    if (tabId !== null && tabId !== undefined)
        localStorage.setItem(`galahad:${basePath}`, tabId)
}
function urlForTab(tabId: string) {
    const qs = Object.entries(route.query)
        .map(
            ([k, v]) =>
                `${k}=${encodeURIComponent(typeof v === "object" ? JSON.stringify(v) : v)}`
        )
        .join("&")
    return `/galahad${basePath}/${tabId}?${qs}`
}
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

            .nav-link {
                font-style: normal;
                text-align: center;
                display: block;
                min-width: 80px;
                padding: 0 20px;
                line-height: inherit;
                text-decoration: none;
                color: black;
                user-select: none;

                &:hover:not(.disabled):not(.active) {
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

                &.active {
                    background-color: var(--int-theme-outline);
                }

                &:active:not(.disabled):not(.active) {
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

        >.header .nav {
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
