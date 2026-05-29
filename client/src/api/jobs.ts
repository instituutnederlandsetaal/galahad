/**
 * API calls for getting existing jobs and job layer results, posting and cancelling jobs, and polling job progress.
 */

import axios, { type AxiosResponse } from "axios"
import type { UUID } from "@/types/corpora"
import type { Job, Progress } from "@/types/jobs"
import { endpoints } from "@/api"

type JobsResponse = AxiosResponse<Job[]>
export type JobResponse = AxiosResponse<Job>
export type JobProgressResponse = AxiosResponse<Progress>
/**
 * Fetch all jobs for a corpus.
 * @param corpus UUID of the corpus.
 */
export function getJobs(corpus: UUID): Promise<JobsResponse> {
    return axios.get(endpoints.jobs.base({ corpus }))
}

/**
 * Post a job to start it. Will return an immediate ProgressResponse.busy=true to give the illusion of a started job.
 * @param corpus UUID of the corpus.
 * @param job Tagger job name.
 */
export function postJob(corpus: UUID, job: string): Promise<AxiosResponse> {
    return axios.post(endpoints.jobs.job({ corpus, job }))
}

export function cancelJob(corpus: UUID, job: string): Promise<AxiosResponse> {
    return axios.delete(endpoints.jobs.job({ corpus, job }))
}

export function getJob(corpus: UUID, job: string): Promise<JobResponse> {
    return axios.get(endpoints.jobs.job({ corpus, job }))
}

export function getJobProgress(corpus: UUID, job: string): Promise<JobProgressResponse> {
    return axios.get(endpoints.jobs.progress({ corpus, job }))
}
