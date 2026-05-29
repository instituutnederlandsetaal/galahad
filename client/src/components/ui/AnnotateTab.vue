<!-- AnnotateTab takes care of error messages for unselected and empty corpora for the subtabs under annotate/* -->
<template>
    <!-- This div fills the background color. -->
    <!-- loading corpora-->
    <GCard v-if="loading" title="Loading corpora">
        <GSpinner />
    </GCard>
    <!-- No corpus selected -->
    <GCard v-else-if="!corpus && !hideCorpusError" title="No corpus selected">
        <GInfo error>
            <p>No corpus has been selected.</p>
            <router-link to="/annotate/corpora">Select a corpus</router-link>
        </GInfo>
    </GCard>
    <!-- No documents in corpus-->
    <GCard v-else-if="!corpus?.documents && !hideDocsError" title="Empty corpus">
        <GInfo error>
            <p>This corpus has no documents.</p>
            <router-link to="/annotate/documents">Upload documents to this corpus</router-link>
        </GInfo>
    </GCard>
    <!-- content -->
    <GCard v-else>
        <template v-if="$slots.title" #title>
            <slot name="title"></slot>
        </template>
        <template v-if="$slots.help" #help>
            <slot name="help"></slot>
        </template>
        <slot></slot>
    </GCard>
</template>

<script setup lang="ts">
import useCorpora from "@/stores/corpora"

const { hideCorpusError = false, hideDocsError = false } = defineProps<{
    hideCorpusError?: boolean
    hideDocsError?: boolean
}>()
const { loading, corpus } = storeToRefs(useCorpora())
</script>
