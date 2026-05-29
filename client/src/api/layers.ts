/**
 * API calls for fetching documents for a corpus, uploading and deleting documents,
 * and downloading the uploaded source document.
 */

import axios, { type AxiosResponse } from "axios"
import type { UUID } from "@/types/corpora"
import type { LayerMetadata } from "@/types/layers"
import { endpoints } from "@/api"

type LayersResponse = AxiosResponse<LayerMetadata[]>

/**
 * Fetch all layers for a corpus.
 * @param corpus UUID of the corpus.
 */
export function getLayers(corpus: UUID): Promise<LayersResponse> {
    return axios.get(endpoints.layers.base({ corpus }))
}

export function removeLayer(corpus: UUID, layer: string): Promise<AxiosResponse> {
    return axios.delete(endpoints.layers.layer({ corpus, layer }))
}
