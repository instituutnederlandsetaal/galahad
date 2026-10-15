/** API for fetching layer comparisons. */

import axios, { type AxiosResponse } from "axios"
import type { UUID } from "@/types/corpora"
import { endpoints } from "@/api"
import type { TermComparison } from "@/types/evaluation"

/**
 * Fetch the layer comparison of a document in two layers.
 * @param corpus Corpus UUID.
 * @param layer Layer name used as hypothesis.
 * @param document Document name.
 * @param reference Layer name used as reference.
 */
export function getDocumentLayerComparison(
    corpus: UUID,
    layer: string,
    document: string,
    reference: string,
): Promise<AxiosResponse<TermComparison[]>> {
    return axios.get(endpoints.evaluation.comparison.base({ corpus, layer, document }, { reference }))
}
