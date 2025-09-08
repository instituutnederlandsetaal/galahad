import stores from "@/stores"
import * as API from "@/api/evaluation"
import * as Utils from "@/api/utils"

/** Used to download the evaluation CSV zip. */
const useEvaluation = defineStore("evaluation", () => {
    const { corpusId } = storeToRefs(stores.useCorpora())
    const { hypothesisId, referenceId } = storeToRefs(stores.useJobSelection())

    const loading = ref<boolean>()

    function downloadCSV(): void {
        loading.value = true
        API.getDownloadEvaluation(corpusId.value, hypothesisId.value, referenceId.value)
            .then(Utils.browserDownloadResponseFile)
            .finally(() => loading.value = false)
    }

    return { downloadCSV, loading }
})

export default useEvaluation
