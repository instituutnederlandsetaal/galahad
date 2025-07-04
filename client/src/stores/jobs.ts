// Libraries & stores

import * as API from "@/api/jobs"
import { jobsPath } from "@/api/jobs"
import type { ProgressResponse } from "@/api/jobs"
import { getDocsAtTaggers } from "@/api/taggers"
import { useAxios } from "@/api/useAxios"
import stores from "@/stores"
// API & Types
import { type Job, SOURCE_LAYER } from "@/types/jobs"

const POLL_INTERVAL = 5000

/**
 * Starts, stops and deletes jobs. Polls for job progress. Fetches available jobs.
 */
const useJobs = defineStore("jobs", () => {
    // Stores
    const errors = stores.useErrors()
    const corporaStore = stores.useCorpora()
    const { corpusId } = storeToRefs(corporaStore)

    // Fields
    // Job statuses for the taggers.
    const url = computed<string | undefined>((): string | undefined =>
        corpusId.value ? jobsPath(corpusId.value) : undefined,
    )
    const { data: jobs, loading, reload } = useAxios<Job[]>(url, [])

    const taggerJobs = computed((): Job[] => jobs.value.filter((i) => i.tagger.id !== SOURCE_LAYER))
    const posting = ref<boolean>()
    const pollers = {} as { [tagger: string]: number }
    const queueSize = ref<number>()

    // Methods
    /**
     * Fetch the progress for the given job. To be used within a poller.
     * @param job Tagger job name.
     */
    function getProgress(job: string, corpus: string): void {
        API.getJobProgress(corpus, job)
            .then((response) => setProgress(job, response))
            .catch((error) => errors.handle(error))
    }

    /**
     * On poll promise resolve, set the progress for the given job.
     */
    const setProgress = (job: string, response: ProgressResponse): void => {
        if (response.request.responseURL.includes(corporaStore.corpusId)) {
            // Only commit the response if it corresponds to the correct corpus
            // This prevents late responses overwriting responses to newer requests
            jobs.value[job].progress = response.data
            // Stop polling if the job is done.
            if (!response.data.busy) {
                stopPolling(job)
                // Displaying the layer preview requires a reload.
                reload()
                // also reload corpora to display the correct number of active and finished jobs
                corporaStore.reload()
            }
        } else {
            // fizzle
        }
    }

    /**
     * Start a continuous progress poller for the given job
     * @param job Tagger job name.
     */
    function startPolling(job: string, corpus: string): void {
        if (!(job in pollers)) {
            pollers[job] = setInterval(
                (job: string) => {
                    getProgress(job, corpus)
                },
                POLL_INTERVAL,
                job,
            )
        }
    }

    /**
     * Stop polling progress for the given job.
     * @param job Tagger job name.
     */
    function stopPolling(job: string): void {
        clearInterval(pollers[job])
        delete pollers[job]
    }

    function tag(job: string): void {
        posting.value = true
        API.postJob(corporaStore.corpusId, job)
            .then((response) => {
                posting.value = false
                // Fake it, because at this point all files will still be 'pending'.
                // isBusy however depends on 'processing', so at this point it will still be false.
                // A future poll will probably set it to true.
                response.data.busy = true
                setProgress(job, response)
                startPolling(job, corporaStore.corpusId) // TODO: this is a problem, because if the state doesn't change, the polling isn't stopped.
                getDocsAtTagger()
            })
            .catch((error) => errors.handle(error))
    }

    function cancel(job: string): void {
        posting.value = true
        API.cancelOrDeleteJob(corporaStore.corpusId, job, false)
            .then((response) => {
                posting.value = false
                setProgress(job, response)
                getDocsAtTagger()
            })
            .catch((error) => errors.handle(error))
    }

    // 'delete' is a reserved keyword
    function remove(job: string): void {
        posting.value = true
        API.cancelOrDeleteJob(corporaStore.corpusId, job, true)
            .then((response) => {
                posting.value = false
                setProgress(job, response)
                getDocsAtTagger()
            })
            .catch((error) => errors.handle(error))
    }

    /**
     * Get the number of documents processing at all taggers.
     */
    function getDocsAtTagger(): void {
        queueSize.value = null
        getDocsAtTaggers()
            .then((response) => {
                queueSize.value = response.data
            })
            .catch((error) => {
                // Ignore
            })
    }

    // Exports
    return {
        // Fields
        jobs,
        taggerJobs,
        loading,
        posting,
        queueSize,
        // Methods
        tag,
        cancel,
        remove,
        reload,
        getDocsAtTagger,
    }
})

export default useJobs
