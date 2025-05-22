// Libraries & stores

import stores from "@/stores"
// API & types
import * as API from "@/api/evaluation"
import * as Utils from "@/api/utils"
import type { Term, TermComparison } from "@/types/evaluation"
import type { UUID } from "@/types/corpora"

// For some reason the terms are undefined sometimes
// We handle it here
export function literalsForTerm(term: Term): string {
	return term.annotations["token"]
}

export function literalsForTermComparison(
	termComparison: TermComparison,
): string {
	// the literals could be different for term1 and term2
	if (
		literalsForTerm(termComparison.hypoTerm) ==
			literalsForTerm(termComparison.refTerm) ||
		literalsForTerm(termComparison.refTerm) == ""
	) {
		return literalsForTerm(termComparison.hypoTerm)
	} else if (literalsForTerm(termComparison.hypoTerm) == "") {
		return literalsForTerm(termComparison.refTerm)
	} else {
		return `MISMATCH: [${literalsForTerm(termComparison.hypoTerm)} — ${literalsForTerm(termComparison.refTerm)}]`
	}
}

/**
 * Used to download the evaluation CSV zip.
 */
const useEvaluation = defineStore("evaluation", () => {
	// Stores
	const app = stores.useApp()
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
			jobSelection.hypothesisJobId,
			jobSelection.referenceJobId,
		)
			.then(Utils.browserDownloadResponseFile)
			.catch((error) =>
				Utils.handleBlobError(error, "download evaluation", app),
			)
			.finally(() => (loading.value = false))
	}

	// Exports
	return { downloadCSV, loading, hypothesis, reference, corpusUUID }
})

export default useEvaluation
