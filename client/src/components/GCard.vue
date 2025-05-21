<template>
    <section class="g-card">
        <header v-if="$slots.title || title || $slots.help">
            <!-- title -->
            <hgroup v-if="title || $slots.title" class="title">
                <h3 class="h3">
                    <slot name="title">{{ title }}</slot>
                </h3>
                <GButton v-if="$slots.help" class="help-btn" title="Help" plain @click="expand = !expand">
                    {{ expand ? "&times;" : "?" }}
                </GButton>
            </hgroup>

            <!-- help -->
            <GInfo v-if="expand && $slots.help">
                <slot name="help"></slot>
                <template v-if="helpSubject" #footer>
                    <HelpLink :subject="helpSubject" />
                </template>
            </GInfo>

            <!-- header -->
            <i class="header">
                <slot name="header"></slot>
            </i>
        </header>

        <!-- content -->
        <div class="content">
            <slot></slot>
        </div>
    </section>
</template>

<script setup lang="ts">
// --- components ---

// --- props ---
const { helpSubject, title } = defineProps<{
    helpSubject?: string
    title?: string
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

    .content {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
    }
}
</style>
