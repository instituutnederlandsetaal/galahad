/**
 * API calls for fetching documents for a corpus, uploading and deleting documents,
 * and downloading the uploaded source document.
 */

import axios, { type AxiosResponse } from "axios"
import type { UUID } from "@/types/corpora"
import type { LayerMetadata } from "@/types/layers"

type LayersResponse = AxiosResponse<LayerMetadata[]>

const layersPath = (corpus: UUID): string => `/corpora/${corpus}/layers`

/**
 * Fetch all layers for a corpus.
 * @param corpus UUID of the corpus.
 */
export function getLayers(corpus: UUID): Promise<LayersResponse> {
    return axios.get(layersPath(corpus))
}