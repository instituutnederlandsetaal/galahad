/**
 * API calls for fetching documents for a corpus, uploading and deleting documents,
 * and downloading the uploaded source document.
 */

// -- api ---
import * as Utils from "@/api/utils"
import type {BlobResponse} from "@/api/utils"
import type {UUID} from "@/types/corpora"
import type {DocumentMetadata} from "@/types/documents"
// --- libraries ---
import axios from "axios"
// --- types ---
import type {AxiosResponse} from "axios"

type DocumentsResponse = AxiosResponse<DocumentMetadata[]>

// --- computed ---
const documentsPath = (corpus: UUID) => `/corpora/${corpus}/documents`
const documentPath = (corpus: UUID, document: string) =>
    `${documentsPath(corpus)}/${document}`
const rawDocumentPath = (corpus: UUID, document: string) =>
    `${documentPath(corpus, document)}/raw`

// --- methods ---
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
) {
    return axios.post(documentsPath(corpus), document, {headers: contentType})
}

/**
 * Delete uploaded document.
 * @param corpus UUID of the corpus.
 * @param document Document name.
 */
export function deleteDocument(corpus: UUID, document: string) {
    return axios.delete(documentPath(corpus, document))
}

/**
 * Download the uploaded source document.
 * @param corpus UUID of the corpus.
 * @param document Document name.
 */
export function getRawDocument(
    corpus: UUID,
    document: string,
): Promise<BlobResponse> {
    return Utils.getBlob(rawDocumentPath(corpus, document))
}
