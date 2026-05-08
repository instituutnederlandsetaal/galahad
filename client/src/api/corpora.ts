/**
 * API calls for creating, updating, deleting a corpus, and fetching the list of corpora.
 */

import axios, { type AxiosResponse } from "axios"
import type { CorpusMetadata, MutableCorpusMetadata, UUID } from "@/types/corpora"

type CorporaResponse = AxiosResponse<CorpusMetadata[]>
type CorpusResponse = AxiosResponse<CorpusMetadata>

export const corporaPath = "/corpora"
const corpusPath = (uuid: UUID): string => `${corporaPath}/${uuid}`

/**
 * Fetch all corpora for which the user has read access:
 * public (datasets), shared (collaborator/viewer), and owned.
 */
export function getCorpora(): Promise<CorporaResponse> {
    return axios.get(corporaPath)
}

/**
 * Fetch a single corpus by UUID.
 * @param uuid UUID of corpus.
 */
export function getCorpus(uuid: UUID): Promise<CorpusResponse> {
    return axios.get(corpusPath(uuid))
}

/**
 * Create a new corpus with the given metadata.
 * @param corpus Metadata of new corpus.
 */
export function postCorpus(corpus: MutableCorpusMetadata): Promise<AxiosResponse<UUID>> {
    return axios.post(corporaPath, corpus)
}

/**
 * Delete a corpus by UUID.
 * @param uuid UUID of corpus to delete.
 */
export function deleteCorpus(uuid: UUID): Promise<AxiosResponse> {
    return axios.delete(corpusPath(uuid))
}

/**
 * Update the metadata of a corpus.
 * @param uuid UUID of corpus to update.
 * @param metadata New metadata.
 */
export function patchCorpus(uuid: UUID, metadata: MutableCorpusMetadata): Promise<AxiosResponse> {
    return axios.patch(corpusPath(uuid), metadata)
}
