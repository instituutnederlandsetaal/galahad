/**
 * API calls for getting existing jobs and job layer results, posting and cancelling jobs, and polling job progress.
 */

import axios, { type AxiosResponse } from "axios"
import type { UUID } from "@/types/corpora"
import type { Job, Progress } from "@/types/jobs"

type JobsResponse = AxiosResponse<Job[]>
type JobResponse = AxiosResponse<Job>
export type ProgressResponse = AxiosResponse<Progress>

const jobsPath = (corpus: UUID): string => `/corpora/${corpus}/jobs`
const jobPath = (corpus: UUID, job: string): string =>
    `/corpora/${corpus}/jobs/${job}`
const jobIsBusyPath = (corpus: UUID, job: string): string =>
    `/corpora/${corpus}/jobs/${job}/isBusy`
const jobProgressPath = (corpus: UUID, job: string): string =>
    `/corpora/${corpus}/jobs/${job}/progress`

/**
 * Fetch all jobs for a corpus.
 * @param corpus UUID of the corpus.
 */
export function getJobs(corpus: UUID): Promise<JobsResponse> {
    return axios.get(jobsPath(corpus), { params: { hasResult: false } })
}

/**
 * Fetch a single job for a corpus.
 * @param corpus UUID of the corpus.
 * @param job Tagger job name.
 */
export function getJob(corpus: UUID, job: string): Promise<JobResponse> {
    return axios.get(jobPath(corpus, job))
}

/**
 * Post a job to start it. Will return an immediate ProgressResponse.busy=true to give the illusion of a started job.
 * @param corpus UUID of the corpus.
 * @param job Tagger job name.
 */
export function postJob(corpus: UUID, job: string): Promise<ProgressResponse> {
    return axios.post(jobPath(corpus, job))
}

/**
 * Cancel or delete a job.
 * @param corpus UUID of the corpus.
 * @param job Tagger job name.
 * @param hard True to delete the job, false to cancel it.
 */
export function cancelOrDeleteJob(
    corpus: UUID,
    job: string,
    hard: boolean
): Promise<ProgressResponse> {
    return axios.delete(jobPath(corpus, job), { params: { hard: hard } })
}

/**
 * Simplified job progress poll.
 */
export function getJobIsBusy(
    corpus: UUID,
    job: string
): Promise<AxiosResponse<boolean>> {
    return axios.get(jobIsBusyPath(corpus, job))
}

/**
 * Poll for job progress.
 */
export function getJobProgress(
    corpus: UUID,
    job: string
): Promise<ProgressResponse> {
    return axios.get(jobProgressPath(corpus, job))
}
