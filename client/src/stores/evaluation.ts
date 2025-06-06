// Libraries & stores

// API & types
import * as API from "@/api/evaluation"
import * as Utils from "@/api/utils"
import stores from "@/stores"
import type { UUID } from "@/types/corpora"
import type { Term, TermComparison } from "@/types/evaluation"

// For some reason the terms are undefined sometimes
// We handle it here
export function literalsForTerm(term: Term): string {
    return term.annotations.token
}

export function literalsForTermComparison(
    termComparison: TermComparison
): string {
    // the literals could be different for term1 and term2
    if (
        literalsForTerm(termComparison.hypoTerm) ===
            literalsForTerm(termComparison.refTerm) ||
        literalsForTerm(termComparison.refTerm) === ""
    ) {
        return literalsForTerm(termComparison.hypoTerm)
    }
    if (literalsForTerm(termComparison.hypoTerm) === "") {
        return literalsForTerm(termComparison.refTerm)
    }
    return `MISMATCH: [${literalsForTerm(termComparison.hypoTerm)} — ${literalsForTerm(termComparison.refTerm)}]`
}

/**
 * Used to download the evaluation CSV zip.
 */
const useEvaluation = defineStore("evaluation", () => {
    // Stores
    const errors = stores.useErrors()
    const corporaStore = stores.useCorpora()
    const jobSelection = stores.useJobSelection()

    // Fields
    const loading = ref(false)
    /** Hypothesis, reference and corpusUUID for which the current evaluations are loaded. */
    const hypothesis = ref(null as string | null)
    const reference = ref(null as string | null)
    const corpusUUID = ref(null as UUID | null)

    // Methods
    function downloadCSV() {
        loading.value = true
        API.getDownloadEvaluation(
            corporaStore.activeUUID,
            jobSelection.hypothesisId,
            jobSelection.referenceId
        )
            .then(Utils.browserDownloadResponseFile)
            .catch(error =>
                Utils.handleBlobError(error, "download evaluation", errors)
            )
            .finally(() => (loading.value = false))
    }

    // Exports
    return { downloadCSV, loading, hypothesis, reference, corpusUUID }
})

export default useEvaluation
