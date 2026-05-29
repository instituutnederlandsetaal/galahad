/**
 * API calls for creating, updating, deleting a corpus, and fetching the list of corpora.
 */

import axios, { type AxiosResponse } from "axios"
import type { CorpusMetadata, MutableCorpusMetadata, UUID } from "@/types/corpora"
import { endpoints } from "@/api"

type CorporaResponse = AxiosResponse<CorpusMetadata[]>
type CorpusResponse = AxiosResponse<CorpusMetadata>

/**
 * Fetch all corpora for which the user has read access:
 * public (datasets), shared (collaborator/viewer), and owned.
 */
export function getCorpora(): Promise<CorporaResponse> {
    return axios.get(endpoints.corpora.base())
}

/**
 * Fetch a single corpus by UUID.
 * @param uuid UUID of corpus.
 */
export function getCorpus(corpus: UUID): Promise<CorpusResponse> {
    return axios.get(endpoints.corpora.corpus({ corpus }))
}

/**
 * Create a new corpus with the given metadata.
 * @param corpus Metadata of new corpus.
 */
export function postCorpus(corpus: MutableCorpusMetadata): Promise<AxiosResponse<UUID>> {
    return axios.post(endpoints.corpora.base(), corpus)
}

/**
 * Delete a corpus by UUID.
 * @param uuid UUID of corpus to delete.
 */
export function deleteCorpus(corpus: UUID): Promise<AxiosResponse> {
    return axios.delete(endpoints.corpora.corpus({ corpus }))
}

/**
 * Update the metadata of a corpus.
 * @param uuid UUID of corpus to update.
 * @param metadata New metadata.
 */
export function updateCorpus(corpus: UUID, metadata: MutableCorpusMetadata): Promise<AxiosResponse> {
    return axios.patch(endpoints.corpora.corpus({ corpus }), metadata)
}
