import stores from "@/stores"
import { type Job } from "@/types/jobs"
import type { SelectOption } from "@/types/ui/select"
import { useRouteQuery } from "@vueuse/router"

/** Stores the current job selection from the <select>. Used in Evaluation & Export. */
const useJobSelection = defineStore("jobSelection", () => {
    // Stores
    const { jobs } = storeToRefs(stores.useJobs())
    const { corpusId } = storeToRefs(stores.useCorpora())

    // Fields
    const hypothesisId = useRouteQuery<string>("hypothesis")
    const referenceId = useRouteQuery<string>("reference")

    // Selectable jobs are jobs that have at least one finished document,
    // or have source annotations (i.e. sourceLayer).
    const options = computed<SelectOption[]>((): SelectOption[] =>
        jobs.value
            .filter((j: Job) => j.progress.finished > 0)
            .map((j: Job) => ({ value: j.tagger.id, text: format(j) })),
    )

    /** Format as displayed in the <select> */
    function format(job: Job): string {
        return `${job.tagger.id} (${job.tagger.description}) [${job.progress.finished}/${job.progress.total} docs]`
    }

    watch(corpusId, () => { hypothesisId.value = undefined; referenceId.value = undefined })

    return { hypothesisId, referenceId, options }
})

export default useJobSelection
