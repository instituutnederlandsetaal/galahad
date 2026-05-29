import * as API from "@/api/evaluation"
import * as Utils from "@/api/utils"
import { plausible } from "@/ts/plausible"
import useCorpora from "@/stores/corpora"
import useLayers from "@/stores/layers"

/** Used to download the evaluation CSV zip. */
const useEvaluation = defineStore("evaluation", () => {
    const { corpusId, corpus } = storeToRefs(useCorpora())
    const { hypothesisId, referenceId, hypothesisLayer, referenceLayer } = storeToRefs(useLayers())

    const loading = ref<boolean>()

    function downloadCSV(): void {
        plausible.evaluationDownloaded(corpus.value, hypothesisLayer.value, referenceLayer.value)
        loading.value = true
        API.getDownloadEvaluation(corpusId.value, hypothesisId.value, referenceId.value)
            .then(Utils.browserDownloadResponseFile)
            .finally(() => (loading.value = false))
    }

    return { downloadCSV, loading }
})

export default useEvaluation
