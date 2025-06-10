/**
 * API calls for fetching evaluation metrics and downloading them as a zip report.
 */

import axios, { type AxiosResponse } from "axios"
import { getBlob, type BlobResponse } from "@/api/utils"
import type { UUID } from "@/types/corpora"
import type {
    ConfusionWrapper,
    DistributionWrapper,
    Metrics,
    TermComparison
} from "@/types/evaluation"
import type {
    DocumentEntities,
    JobEntities,
    JobsEntities
} from "@/types/evaluation/entities"

type ConfusionResponse = AxiosResponse<ConfusionWrapper>
type DistributionResponse = AxiosResponse<DistributionWrapper>
type MetricsResponse = AxiosResponse<Metrics>
type DocumentEntitiesResponse = AxiosResponse<DocumentEntities>
type JobEntitiesResponse = AxiosResponse<JobEntities>
type JobsEntitiesResponse = AxiosResponse<JobsEntities>

const evaluationPath = (corpus: UUID): string => `/corpora/${corpus}/evaluation`
export const confusionPath = (corpus: UUID): string =>
    `${evaluationPath(corpus)}/confusion`
const confusionSamplesPath = (corpus: UUID): string =>
    `${confusionPath(corpus)}/download`
export const distributionPath = (corpus: UUID): string =>
    `${evaluationPath(corpus)}/distribution`
export const metricsPath = (corpus: UUID): string =>
    `${evaluationPath(corpus)}/metrics`
const metricsSamplesPath = (corpus: UUID): string =>
    `${metricsPath(corpus)}/download`
const downloadPath = (corpus: UUID): string =>
    `${evaluationPath(corpus)}/download`
const documentLayerComparisonPath = (
    corpus: UUID,
    job: string,
    document: string
): string => `/corpora/${corpus}/jobs/${job}/documents/${document}/evaluation`
const documentEntitiesPath = (
    corpus: UUID,
    job: string,
    document: string
): string => `/corpora/${corpus}/jobs/${job}/documents/${document}/entities`
const jobEntitiesPath = (corpus: UUID, job: string): string =>
    `${evaluationPath(corpus, job)}/entities`
const jobsEntitiesPath = (corpus: UUID): string =>
    `/corpora/${corpus}/evaluation/entities`
/**
 * Fetch term frequency distribution.
 * @param corpus UUID of the corpus.
 * @param hypothesis Tagging job name as hypothesis layer.
 */
export function getDistribution(
    corpus: UUID,
    hypothesis: string
): Promise<DistributionResponse> {
    return axios.get(distributionPath(corpus), { params: { hypothesis } })
}

/**
 * Fetch term confusion matrix.
 * @param corpus UUID of the corpus.
 * @param hypothesis Tagger job name as hypothesis layer.
 * @param reference Tagger job name as reference layer.
 */
export function getConfusion(
    corpus: UUID,
    hypothesis: string,
    reference: string
): Promise<ConfusionResponse> {
    return axios.get(confusionPath(corpus, hypothesis), {
        params: { reference }
    })
}

/**
 * Fetch Lemma & PoS accuracy metrics.
 * @param corpus UUID of the corpus.
 * @param hypothesis Tagger job name as hypothesis layer.
 * @param reference Tagger job name as reference layer.
 */
export function getMetrics(
    corpus: UUID,
    hypothesis: string,
    reference: string
): Promise<MetricsResponse> {
    return axios.get(metricsPath(corpus, hypothesis), { params: { reference } })
}

/**
 * Download evaluation zip.
 * @param corpus UUID of the corpus.
 * @param hypothesis Tagger job name as hypothesis layer.
 * @param reference  Tagger job name as reference layer.
 */
export function getDownloadEvaluation(
    corpus: UUID,
    hypothesis: string,
    reference: string
): Promise<BlobResponse> {
    return getBlob(downloadPath(corpus, hypothesis), {
        params: { reference }
    })
}

/**
 * Download confusion entries as zip.
 * @param corpus UUID of the corpus.
 * @param hypothesis Tagger job name as hypothesis layer.
 * @param reference  Tagger job name as reference layer.
 * @param hypoAnnot PoS tag of the hypothesis layer to filter on.
 * @param refAnnot PoS tag of the reference layer to filter on.
 */
export function getConfusionSamples(
    corpus: UUID,
    hypothesis: string,
    reference: string,
    hypoFilter: string,
    refFilter: string,
    annotationType: string
): Promise<BlobResponse> {
    return getBlob(confusionSamplesPath(corpus, hypothesis), {
        params: { reference, hypoFilter, refFilter, annotationType }
    })
}

/**
 * Download metrics samples as zip.
 * @param corpus UUID of the corpus.
 * @param hypothesis Tagger job name as hypothesis layer.
 * @param reference  Tagger job name as reference layer.
 * @param setting Setting for the metrics. E.g. 'posByPos'
 * @param classType Class type for the metrics. E.g. 'truePositive'.
 * @param group Group for the metrics. E.g. 'pos' or 'lemma'.
 */
export function getMetricsSamples(
    corpus: UUID,
    hypothesis: string,
    reference: string,
    setting: string,
    classType: string,
    group?: string
): Promise<BlobResponse> {
    const params: Record<string, string> = {
        reference,
        metricsType: setting,
        class: classType
    }
    if (group) {
        params.group = group
    }
    return getBlob(metricsSamplesPath(corpus, hypothesis), { params })
}

/**
 * Fetch the document layer comparison of a single document comparing the job layer to the reference layer.
 * @param corpus UUID of the corpus.
 * @param job Job name.
 * @param document Document name.
 * @param reference Reference layer name.
 * @returns Document layer comparison.
 */
export function getDocumentLayerComparison(
    corpus: UUID,
    job: string,
    document: string,
    reference: string
): Promise<AxiosResponse<TermComparison[]>> {
    return axios.get(documentLayerComparisonPath(corpus, job, document), {
        params: { reference }
    })
}

export function getDocumentEntities(
    corpus: UUID,
    job: string,
    document: string
): Promise<DocumentEntitiesResponse> {
    return axios.get(documentEntitiesPath(corpus, job, document))
}

export function getJobEntities(
    corpus: UUID,
    job: string
): Promise<JobEntitiesResponse> {
    return axios.get(jobEntitiesPath(corpus, job))
}

export function getJobsEntities(corpus: UUID): Promise<JobsEntitiesResponse> {
    return axios.get(jobsEntitiesPath(corpus))
}
