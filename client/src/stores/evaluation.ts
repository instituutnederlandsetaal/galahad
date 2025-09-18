import stores from "@/stores"
import * as API from "@/api/evaluation"
import * as Utils from "@/api/utils"
import { plausible } from "@/ts/plausible"

/** Used to download the evaluation CSV zip. */
const useEvaluation = defineStore("evaluation", () => {
    const { corpusId, corpus } = storeToRefs(stores.useCorpora())
    const { hypothesisId, referenceId, hypothesisJob, referenceJob } = storeToRefs(stores.useJobSelection())

    const loading = ref<boolean>()

    function downloadCSV(): void {
        plausible.evaluationDownloaded(corpus.value, hypothesisJob.value, referenceJob.value)
        loading.value = true
        API.getDownloadEvaluation(corpusId.value, hypothesisId.value, referenceId.value)
            .then(Utils.browserDownloadResponseFile)
            .finally(() => loading.value = false)
    }

    return { downloadCSV, loading }
})

export default useEvaluation
