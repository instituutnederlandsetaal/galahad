<template>
    <GTabs
        class="level-2"
        basePath="/annotate"
        :tabs="[
            { id: 'corpora', title: 'Corpora' },
            { id: 'documents', title: 'Documents', disabled: !corporaStore.activeCorpus },
            { id: 'jobs', title: 'Jobs', disabled: !corporaStore.hasDocs },
            { id: 'evaluate', title: 'Evaluate', disabled: !corporaStore.hasDocs },
            { id: 'export', title: 'Export', disabled: !corporaStore.hasDocs },
        ]" />
</template>

<script setup lang="ts">
// Libraries & stores

import router from "@/router"
import stores from "@/stores"

// Stores
const userStore = stores.useUser()
const corporaStore = stores.useCorpora()

// When reloading the page in any of the subtabs, the corpus UUID will be set,
// but computing activeCorpus also needs the corpora to be retrieved.
onMounted(() => {
	// The corpora tab is an exception, it already reloads corporaStore by itself.
	if (!router.currentRoute.value.path.includes("corpora")) corporaStore.reload()
})
</script>
