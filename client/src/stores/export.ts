import * as API from "@/api/export"
import * as Utils from "@/api/utils"
import stores from "@/stores"
import { plausible } from "@/ts/plausible"
import { Format } from "@/types/documents"
import type { SelectOption } from "@/types/ui/select"

/** Used to download exported corpora. */
const useExport = defineStore("exportStore", () => {
    // Stores
    const corporaStore = stores.useCorpora()
    const jobSelection = stores.useJobSelection()
    const errors = stores.useErrors()

    // Fields
    const loading = ref<boolean>()
    const format = ref<Format>()
    const options: SelectOption[] = [
        { value: Format.CONLLU, text: "CoNLL-U (Universal Dependencies)" },
        { value: Format.FOLIA, text: "FoLiA (Format for Linguistic Annotation)" },
        { value: Format.NAF, text: "NAF (NLP Annotation Format) " },
        { value: Format.TEI_P5, text: "TEI P5 (Text Encoding Initiative)" },
        { value: Format.TSV, text: "TSV (Tab-separated values)" },
        { value: Format.TXT, text: "TXT (Plain text, tokens only)" },
        { value: Format.JSON, text: "JSON (GaLAHaD internal format)" },
    ]

    // Methods
    function convert(shouldMerge: boolean, posHeadOnly: boolean): void {
        if (shouldMerge) {
            merge(posHeadOnly)
            return
        }
        loading.value = true
        plausible.corpusExported(corporaStore.corpus, jobSelection.hypothesisId, format.value, shouldMerge, posHeadOnly)
        API.convertCorpus(corporaStore.corpusId, jobSelection.hypothesisId, format.value, posHeadOnly)
            .then(Utils.browserDownloadResponseFile)
            .finally(() => (loading.value = false))
    }

    function merge(posHeadOnly: boolean): void {
        loading.value = true
        plausible.corpusExported(corporaStore.corpus, jobSelection.hypothesisId, format.value, true, posHeadOnly)
        API.mergeCorpus(corporaStore.corpusId, jobSelection.hypothesisId, format.value, posHeadOnly)
            .then(Utils.browserDownloadResponseFile)
            .finally(() => (loading.value = false))
    }

    return { format, options, loading, convert }
})

export default useExport
