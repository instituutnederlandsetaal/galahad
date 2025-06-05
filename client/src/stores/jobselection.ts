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
    const corporaStore = stores.useCorpora()
    const documentsStore = stores.useDocuments()

    // Fields
    const hypothesisJobId = useRouteQuery("hypothesis")
    const referenceJobId = useRouteQuery("reference")
    // Set to true once we know the jobs exist in selectableJobs.
    // (which requires waiting on jobs & docs to load)
    const selectionsValid = ref(false)
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
    const referenceJob = computed<Job | undefined>(() => {
        return referenceJobId.value
            ? jobsStore.jobs[referenceJobId.value]
            : undefined
    })
    const hypothesisJob = computed<Job | undefined>(() => {
        return hypothesisJobId.value
            ? jobsStore.jobs[hypothesisJobId.value]
            : undefined
    })

    // // Watches
    watch(
        () => corporaStore.activeUUID,
        (newValue, oldValue) => {
            if (oldValue !== newValue && oldValue) {
                hypothesisJobId.value = undefined
                referenceJobId.value = undefined
            }
        }
    )
    // /** Remove invalid job selections on loading jobs & loading docs (the latter for sourceLayer annotations).*/
    // watch([() => jobsStore.loading, () => documentsStore.loading], () => {
    //     validateJobSelections()
    // })

    // Methods
    /** Remove any invalid job selections, either non-existing names or jobs that have no layer  */
    function validateJobSelections() {
        const jobsExist: boolean =
            !jobsStore.loading && Object.keys(jobsStore.jobs).length > 0
        const docsExist: boolean =
            !documentsStore.loading && documentsStore.documents.length > 0
        if (jobsExist && docsExist) {
            if (
                !selectableJobs.value
                    .map(job => job.key)
                    .includes(hypothesisJobId.value)
            ) {
                hypothesisJobId.value = undefined
            }
            if (
                !selectableJobs.value
                    .map(job => job.key)
                    .includes(referenceJobId.value)
            ) {
                referenceJobId.value = undefined
            }
            selectionsValid.value = true
        }
    }
    /** Format as displayed in the <select> */
    function formatJobString(job: Job) {
        let finished = job.progress.finished
        if (job.tagger.id === SOURCE_LAYER) {
            finished = documentsStore.numSourceAnnotations
            return `source annotations [${finished}/${job.progress.total} docs]`
        }
        return `${job.tagger.id} (${job.tagger.description}) [${finished}/${job.progress.total} docs]`
    }
    function setHypothesisJobId(id: string) {
        if (Object.keys(jobsStore.jobs)?.includes(id))
            hypothesisJobId.value = id
    }
    function setReferenceJobId(id: string) {
        if (Object.keys(jobsStore.jobs)?.includes(id)) referenceJobId.value = id
    }

    // Exports
    return {
        // Fields
        tagsetsMismatch,
        hypothesisJobId,
        referenceJobId,
        selectableJobs,
        selectionsValid,
        // Methods
        setHypothesisJobId,
        setReferenceJobId
    }
})

export default useJobSelection
