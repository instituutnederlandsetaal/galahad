import stores from "@/stores"
import { type Job } from "@/types/jobs"
import type { SelectOption } from "@/types/ui/select"
import { useRouteQuery } from "@vueuse/router"

/** Stores the current job selection from the <select>. Used in Evaluation & Export. */
const useJobSelection = defineStore("jobSelection", () => {
    const { jobs } = storeToRefs(stores.useJobs())
    const { corpusId } = storeToRefs(stores.useCorpora())

    const hypothesisId = useRouteQuery<string>("hypothesis")
    const referenceId = useRouteQuery<string>("reference")
    const hypothesisJob = computed<Job>(() => jobs.value.find((j: Job) => j.tagger.name === hypothesisId.value))
    const referenceJob = computed<Job>(() => jobs.value.find((j: Job) => j.tagger.name === referenceId.value))

    const options = computed<SelectOption[]>((): SelectOption[] =>
        jobs.value
            // Selectable jobs have at least one finished document.
            .filter((j: Job) => j.progress.finished > 0)
            .map((j: Job) => ({ value: j.tagger.name, text: format(j) })),
    )

    /** Format as displayed in the <select> */
    function format(job: Job): string {
        return `${job.tagger.name} (${job.tagger.description}) [${job.progress.finished}/${job.progress.total} docs]`
    }

    watch(corpusId, () => { hypothesisId.value = undefined; referenceId.value = undefined })

    return { hypothesisId, referenceId, hypothesisJob, referenceJob, options }
})

export default useJobSelection
