import stores from "@/stores"
import { type Job, SOURCE_LAYER } from "@/types/jobs"
import type { SelectOption } from "@/types/ui/select"
import { useRouteQuery } from "@vueuse/router"

/**
 * Stores the current job selection from the <select>. Used in Evaluation & Export.
 */
const useJobSelection = defineStore("jobSelection", () => {
    // Stores
    const jobsStore = stores.useJobs()
    const corpora = stores.useCorpora()
    const documentsStore = stores.useDocuments()

    // Fields
    const hypothesisId = useRouteQuery<string>("hypothesis")
    const referenceId = useRouteQuery<string>("reference")
    const tagsetsMismatch = computed(() => {
        return (
            hypothesisJob.value?.tagger.tagset !==
            referenceJob.value?.tagger.tagset
        )
    })
    // Selectable jobs are jobs that have at least one finished document,
    // or have source annotations (i.e. sourceLayer).
    const selectableJobs = computed<SelectOption[]>(() => {
        const jobs = jobsStore.jobs
        if (!jobs) return []
        return (
            Object.keys(jobs)
                ?.filter(job => jobs[job].progress.finished > 0)
                // Filter out sourceLayer if no documents have source annotations.
                .filter(
                    (job: string) =>
                        !(
                            job === SOURCE_LAYER &&
                            !documentsStore.numSourceAnnotations
                        )
                )
                ?.map(job => {
                    return {
                        value: job,
                        text: formatJobString(jobs[job])
                    }
                })
        )
    })

    // Private Fields
    const referenceJob = computed<Job>(() => jobsStore.jobs[referenceId.value])
    const hypothesisJob = computed<Job>(
        () => jobsStore.jobs[hypothesisId.value]
    )

    watch(
        () => corpora.activeUUID,
        () => {
            hypothesisId.value = undefined
            referenceId.value = undefined
        }
    )

    /** Format as displayed in the <select> */
    function formatJobString(job: Job) {
        let finished = job.progress.finished
        if (job.tagger.id === SOURCE_LAYER) {
            finished = documentsStore.numSourceAnnotations
            return `source annotations [${finished}/${job.progress.total} docs]`
        }
        return `${job.tagger.id} (${job.tagger.description}) [${finished}/${job.progress.total} docs]`
    }
    function sethypothesisId(id: string) {
        if (Object.keys(jobsStore.jobs)?.includes(id)) hypothesisId.value = id
    }
    function setreferenceId(id: string) {
        if (Object.keys(jobsStore.jobs)?.includes(id)) referenceId.value = id
    }

    return {
        tagsetsMismatch,
        hypothesisId,
        referenceId,
        selectableJobs,
        sethypothesisId,
        setreferenceId
    }
})

export default useJobSelection
