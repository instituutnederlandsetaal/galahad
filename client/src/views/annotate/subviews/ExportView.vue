<template>
    <AnnotateTab :hidePermissionsError="true">
        <GCard :title="`Export corpus ${corporaStore.activeCorpus?.name}`" helpSubject="export">
            <template #help>
                <component :is="help.export"></component>
            </template>
            <JobSelect customTitle="Annotation layer" />
            <GCard title="Download as format">
                <div id="center">
                    <FileFormatInput v-model="exportStore.format" />

                    <div>
                        <GInput type="checkbox" v-model="posHeadOnly"
                            >Export part of speech<br />without features</GInput
                        >
                        <GInput type="checkbox" v-if="showMergeOption" v-model="shouldMerge">Merge</GInput>
                    </div>

                    <template v-if="showMergeOption">
                        <GInfo>
                            You have uploaded
                            <b>{{ formatToHumanReadable(exportStore.format) }}</b> files to this corpus and you are now
                            exporting <b>{{ formatToHumanReadable(exportStore.format) }}</b
                            >. <br />
                            It is possible to insert the annotation layer into the original file. This will retain the
                            original encoding.
                            <br /><br />
                            If you do not choose the merge option, your export will ignore the original encoding of your
                            uploaded document.
                        </GInfo>

                        <GInfo v-if="hasTeiP5Legacy && shouldMerge" style="max-width: 850px">
                            <h4 style="margin-top: 0">Special notice for <b>TEI P5 legacy</b></h4>
                            <TeiP5LegacyWarning />
                        </GInfo>
                    </template>

                    <DownloadButton
                        wide
                        @click="exportStore.convert(shouldMerge, posHeadOnly)"
                        :disabled="exportStore.loading || !exportStore.linksAreValid" />

                    <GInfo v-if="exportStore.loading" spinner>
                        Please wait while your export is being processed.
                    </GInfo>
                </div>
            </GCard>
        </GCard>
    </AnnotateTab>
</template>

<script setup lang="ts">
// Libraries & stores

import stores from "@/stores"
// Api & types
import { Format } from "@/types/documents"

import help from "@/components/help"
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
        format === Format.Tei_p5 ||
        format === Format.Tsv ||
        format === Format.Folia ||
        format === Format.Conllu
    const formatInCorpus = documentsStore.containsFormat(format)
    return formatIsMergeable && formatInCorpus
})
const hasTeiP5Legacy = computed(() =>
    documentsStore.available.some(i => i.format === Format.Tei_p5_legacy),
)

// Methods
function formatToHumanReadable(format: Format): string {
    switch (format) {
        case Format.Tei_p5:
        case Format.Tei_p5_legacy:
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
.content-wrapper {
    text-align: center;
}

:deep(#center) {
    display: flex;
    flex-direction: column;
    gap: 10px;
    align-content: center;
    align-items: center;
}
</style>
