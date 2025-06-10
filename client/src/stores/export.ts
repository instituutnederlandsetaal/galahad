// Libraries & stores

// Types & API
import * as API from "@/api/export"
import * as Utils from "@/api/utils"
import stores from "@/stores"
import { plausible } from "@/ts/plausible"
import type { Format } from "@/types/documents"

/**
 * Used to download exported corpora.
 */
const useExport = defineStore("exportStore", () => {
    // Stores
    const corporaStore = stores.useCorpora()
    const errors = stores.useErrors()
    const jobSelection = stores.useJobSelection()

    // Fields
    const loading = ref<boolean>()
    const format = ref<Format>()

    // Methods
    function convert(shouldMerge: boolean, posHeadOnly: boolean): void {
        if (shouldMerge) {
            merge(posHeadOnly)
            return
        }
        loading.value = true
        plausible.corpusExported(
            corporaStore.corpus,
            jobSelection.hypothesisId,
            format.value,
            shouldMerge,
            posHeadOnly
        )
        API.convertCorpus(
            corporaStore.corpusId,
            jobSelection.hypothesisId,
            format.value,
            posHeadOnly
        )
            .then(Utils.browserDownloadResponseFile)
            .catch(res => Utils.handleBlobError(res, "convert corpus", errors))
            .finally(() => (loading.value = false))
    }

    function merge(posHeadOnly: boolean): void {
        loading.value = true
        plausible.corpusExported(
            corporaStore.corpus,
            jobSelection.hypothesisId,
            format.value,
            true,
            posHeadOnly
        )
        API.mergeCorpus(
            corporaStore.corpusId,
            jobSelection.hypothesisId,
            format.value,
            posHeadOnly
        )
            .then(Utils.browserDownloadResponseFile)
            .catch(res => Utils.handleBlobError(res, "merge corpus", errors))
            .finally(() => (loading.value = false))
    }
    // Exports
    return {
        // Fields
        format,
        loading,
        // Methods
        convert
    }
})

export default useExport
