<template>
    <GTabs
        class="level-2"
        basePath="/annotate"
        :tabs="[
            { id: 'corpora', title: 'Corpora' },
            { id: 'documents', title: 'Documents', disabled: !corpus },
            { id: 'jobs', title: 'Jobs', disabled: !corpus?.documents },
            { id: 'evaluate', title: 'Evaluate', disabled: !corpus?.documents },
            { id: 'export', title: 'Export', disabled: !corpus?.documents },
        ]"
    />
</template>

<script setup lang="ts">
import useCorpora from "@/stores/corpora"
import useDocuments from "@/stores/documents"
import useJobs from "@/stores/jobs"
import useLayers from "@/stores/layers"

const { corpus } = storeToRefs(useCorpora())

const { reload: reloadCorpora } = useCorpora()
const { reload: reloadLayers } = useLayers()
const { reload: reloadDocuments } = useDocuments()
const { reload: reloadJobs } = useJobs()

onMounted(reloadCorpora)
onMounted(reloadLayers)
onMounted(reloadDocuments)
onMounted(reloadJobs)
</script>
