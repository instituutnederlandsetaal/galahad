<template>
    <section class="g-card">
        <header v-if="$slots.title || title || $slots.help" class="header">
            <hgroup v-if="$slots.title || title" class="title">
                <h3 class="h3">
                    <slot name="title">{{ title }}</slot>
                </h3>
                <GButton v-if="$slots.help" class="help-btn" title="Help" plain @click="expand = !expand">
                    {{ expand ? "&times;" : "?" }}
                </GButton>
            </hgroup>
            <GInfo v-if="expand && $slots.help">
                <slot name="help"></slot>
                <template v-if="helpSubject" #footer>
                    <HelpLink :subject="helpSubject" />
                </template>
            </GInfo>
        </header>
        <article v-if="article" class="content article">
            <slot></slot>
        </article>
        <div v-else class="content">
            <slot></slot>
        </div>
    </section>
</template>

<script setup lang="ts">
// --- props ---
const { helpSubject, title } = defineProps<{
	helpSubject?: string
	title?: string
	article?: boolean
}>()

// --- data ---
const expand = ref(false)
</script>

<style scoped lang="scss">
.g-card {
    background-color: var(--white);
    padding: 1em;
    padding-top: 0;
    min-width: 250px;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1rem;

    .header {
        display: flex;
        flex-direction: column;
        gap: 1rem;

        .title {
            text-align: center;

            .h3 {
                display: inline-block;
            }

            .help-btn.plain {
                border: 1px solid var(--int-grey);
                padding: 0 0.6em;
                font-size: 1.5em;
                cursor: help;
                width: 28px;
                justify-content: center;
                font-weight: bold;
                margin-left: 0.5em;
            }
        }
    }

    .content {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 1rem;

        &.article {
            max-width: 800px;
            align-items: start;
        }
    }
}
</style>
