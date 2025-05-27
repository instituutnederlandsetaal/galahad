<template>
    <AnnotateTab :hidePermissionsError="true">
        <GCard :title="`Export corpus ${corporaStore.activeCorpus?.name}`" helpLink="export">
            <template #help>
                <ExportHelp />
            </template>

            <form @submit.prevent class="form">
                <JobSelect customTitle="Annotation layer" />

                <FileFormatInput />

                <GCheckBox v-model="posHeadOnly">
                    Export part of speech without features
                </GCheckBox>

                <GCheckBox v-if="showMergeOption" v-model="shouldMerge">
                    Merge encoding
                </GCheckBox>

                <DownloadButton class="download" wide :disabled="exportStore.loading || !exportStore.linksAreValid"
                    @click="exportStore.convert(shouldMerge, posHeadOnly)" />
            </form>

            <GInfo v-if="exportStore.loading" spinner>
                Please wait while your export is being processed.
            </GInfo>

            <template v-if="showMergeOption">
                <GInfo>
                    <p>
                        You have uploaded
                        <b>{{ formatToHumanReadable(exportStore.format) }}</b> files to this corpus and you
                        are now
                        exporting <b>{{ formatToHumanReadable(exportStore.format) }}</b>.
                        It is possible to insert the annotation layer into the original file. This will
                        retain the
                        original encoding.
                    </p>
                    <p>
                        If you do not choose the merge option, your export will ignore the original encoding
                        of your
                        uploaded document.
                    </p>
                </GInfo>

                <GInfo v-if="hasTeiP5Legacy && shouldMerge">
                    <h4>Special notice for <b>TEI P5 legacy</b></h4>
                    <TeiP5LegacyWarning />
                </GInfo>
            </template>
        </GCard>
    </AnnotateTab>
</template>

<script setup lang="ts">
// Libraries & stores

import stores from "@/stores"
// Api & types
import { Format } from "@/types/documents"

import TeiP5LegacyWarning from "@/views/help/subviews/formats/TeiP5LegacyWarning.vue"

// Stores
const corporaStore = stores.useCorpora()
const jobsStore = stores.useJobs()
const exportStore = stores.useExport()
const documentsStore = stores.useDocuments()

// Fields
const posHeadOnly = ref(false)
const shouldMerge = ref(true)
const showMergeOption = computed(() => {
    const format = exportStore.format
    const formatIsMergeable =
        format === Format.TEI_P5 ||
        format === Format.TSV ||
        format === Format.FOLIA ||
        format === Format.CONLLU
    const formatInCorpus = documentsStore.containsFormat(format)
    return formatIsMergeable && formatInCorpus
})
const hasTeiP5Legacy = computed(() =>
    documentsStore.available.some(i => i.format === Format.TEI_P5_LEGACY),
)

// Methods
function formatToHumanReadable(format: Format): string {
    switch (format) {
        case Format.TEI_P5:
        case Format.TEI_P5_LEGACY:
            return "TEI P5"
        default:
            return format
    }
}

// Watchers
// Load jobs list once. jobSelectionStore takes care of the selected job.
onMounted(() => {
    jobsStore.reload()
    // We also need to load the documents, in order to determine the presence of TEI files.
    documentsStore.reloadDocumentsForCorpus(corporaStore.activeUUID)
})
</script>

<style scoped lang="scss">
.form {
    display: flex;
    flex-direction: column;
    gap: 1rem;
    align-items: start;

    .download {
        align-self: center;
    }
}
</style>
