import stores from "@/stores"
import { type Job, SOURCE_LAYER } from "@/types/jobs"
import type { SelectOption } from "@/types/ui/select"
import { useRouteQuery } from "@vueuse/router"

/**
 * Stores the current job selection from the <select>. Used in Evaluation & Export.
 */
const useJobSelection = defineStore("jobSelection", () => {
    // Stores
    const { jobs } = storeToRefs(stores.useJobs())
    const corpora = stores.useCorpora()
    const documentsStore = stores.useDocuments()

    // Fields
    const hypothesisId = useRouteQuery<string>("hypothesis")
    const referenceId = useRouteQuery<string>("reference")

    // Selectable jobs are jobs that have at least one finished document,
    // or have source annotations (i.e. sourceLayer).
    const options = computed<SelectOption[]>((): SelectOption[] =>
        jobs.value
            .filter((j: Job) => j.progress.finished > 0)
            .map((j: Job) => ({ value: j.tagger.id, text: formatJobString(j) }))
    )

    watch(
        () => corpora.corpusId,
        () => {
            hypothesisId.value = undefined
            referenceId.value = undefined
        }
    )

    /** Format as displayed in the <select> */
    function formatJobString(job: Job): string {
        const total = job.progress.total
        let finished = job.progress.finished
        if (job.tagger.id === SOURCE_LAYER) {
            finished = documentsStore.numSourceAnnotations
            return `source annotations [${finished}/${total} docs]`
        }
        return `${job.tagger.id} (${job.tagger.description}) [${finished}/${total} docs]`
    }

    return {
        hypothesisId,
        referenceId,
        options
    }
})

export default useJobSelection
