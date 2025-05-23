/**
 * API calls for fetching evaluation metrics and downloading them as a zip report.
 */

// --- api ---
import * as Utils from "@/api/utils"
import type {BlobResponse} from "@/api/utils"
import type {UUID} from "@/types/corpora"
import type {
    ConfusionWrapper,
    DistributionWrapper,
    Metrics,
    TermComparison,
} from "@/types/evaluation"
// --- libraries ---
import axios from "axios"
// --- types ---
import type {AxiosResponse} from "axios"

type ConfusionResponse = AxiosResponse<ConfusionWrapper>
type DistributionResponse = AxiosResponse<DistributionWrapper>
type MetricsResponse = AxiosResponse<Metrics>

// --- computed ---
const evaluationPath = (corpus: UUID, hypothesis: string) =>
    `/corpora/${corpus}/jobs/${hypothesis}/evaluation`
const confusionPath = (corpus: UUID, hypothesis: string) =>
    `${evaluationPath(corpus, hypothesis)}/confusion`
const confusionSamplesPath = (corpus: UUID, hypo: string) =>
    `${evaluationPath(corpus, hypo)}/confusion/download`
const distributionPath = (corpus: UUID, hypothesis: string) =>
    `${evaluationPath(corpus, hypothesis)}/distribution`
const metricsPath = (corpus: UUID, hypothesis: string) =>
    `${evaluationPath(corpus, hypothesis)}/metrics`
const metricsSamplesPath = (corpus: UUID, hypo: string) =>
    `${evaluationPath(corpus, hypo)}/metrics/download`
const downloadPath = (corpus: UUID, hypothesis: string) =>
    `${evaluationPath(corpus, hypothesis)}/download`
const documentLayerComparisonPath = (
    corpus: UUID,
    job: string,
    document: string,
) => `/corpora/${corpus}/jobs/${job}/documents/${document}/evaluation`

// --- methods ---
/**
 * Fetch term frequency distribution.
 * @param corpus UUID of the corpus.
 * @param hypothesis Tagging job name as hypothesis layer.
 */
export function getDistribution(
    corpus: UUID,
    hypothesis: string,
): Promise<DistributionResponse> {
    return axios.get(distributionPath(corpus, hypothesis))
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
    reference: string,
): Promise<ConfusionResponse> {
    return axios.get(confusionPath(corpus, hypothesis), {
        params: {reference},
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
    reference: string,
): Promise<MetricsResponse> {
    return axios.get(metricsPath(corpus, hypothesis), {params: {reference}})
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
    reference: string,
): Promise<BlobResponse> {
    return Utils.getBlob(downloadPath(corpus, hypothesis), {
        params: {reference},
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
    annotationType: string,
): Promise<BlobResponse> {
    return Utils.getBlob(confusionSamplesPath(corpus, hypothesis), {
        params: {reference, hypoFilter, refFilter, annotationType},
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
    group?: string,
): Promise<BlobResponse> {
    const params: Record<string, string> = {
        reference,
        metricsType: setting,
        class: classType,
    }
    if (group) {
        params.group = group
    }
    return Utils.getBlob(metricsSamplesPath(corpus, hypothesis), {params})
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
    reference: string,
): Promise<AxiosResponse<TermComparison[]>> {
    return axios.get(documentLayerComparisonPath(corpus, job, document), {
        params: {reference},
    })
}
