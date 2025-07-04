/**
 * API calls for fetching documents for a corpus, uploading and deleting documents,
 * and downloading the uploaded source document.
 */

import axios, { type AxiosResponse } from "axios"
import { getBlob, type BlobResponse } from "@/api/utils"
import type { UUID } from "@/types/corpora"
import type { DocumentMetadata } from "@/types/documents"

type DocumentsResponse = AxiosResponse<DocumentMetadata[]>

export const documentsPath = (corpus: UUID): string => `/corpora/${corpus}/documents`
const documentPath = (corpus: UUID, document: string): string => `${documentsPath(corpus)}/${document}`
const rawDocumentPath = (corpus: UUID, document: string): string => `${documentPath(corpus, document)}/download`

/**
 * Fetch all documents for a corpus.
 * @param corpus UUID of the corpus.
 */
export function getDocuments(corpus: UUID): Promise<DocumentsResponse> {
    return axios.get(documentsPath(corpus))
}

/**
 * Upload new document.
 * @param corpus UUID of the corpus.
 * @param document Document name.
 * @param contentType Content type of the document. Must be explicitly set for tsv-files. Others are induced.
 */
export function postDocument(
    corpus: UUID,
    document: FormData,
    contentType?: Record<string, string>,
): Promise<AxiosResponse> {
    return axios.post(documentsPath(corpus), document, { headers: contentType })
}

/**
 * Delete uploaded document.
 * @param corpus UUID of the corpus.
 * @param document Document name.
 */
export function deleteDocument(corpus: UUID, document: string): Promise<AxiosResponse> {
    return axios.delete(documentPath(corpus, document))
}

/**
 * Download the uploaded source document.
 * @param corpus UUID of the corpus.
 * @param document Document name.
 */
export function getRawDocument(corpus: UUID, document: string): Promise<BlobResponse> {
    return getBlob(rawDocumentPath(corpus, document))
}
