/**
 * API calls for exporting corpora and documents.
 * Either converted to a certain format or merged with their original file if the format supports it.
 */

// --- api ---
import * as Utils from "@/api/utils"
// --- types ---
import type { BlobResponse } from "@/api/utils"
import type { UUID } from "@/types/corpora"
import { Format } from "@/types/documents"

// --- computed ---
const convertCorpusPath = (corpus: UUID, job: string, format: Format, posHeadOnly: boolean) =>
    `/corpora/${corpus}/jobs/${job}/export/convert?format=${format}&posHeadOnly=${posHeadOnly}`
const mergeCorpusPath = (corpus: UUID, job: string, format: Format, posHeadOnly: boolean) =>
    `/corpora/${corpus}/jobs/${job}/export/merge?format=${format}&posHeadOnly=${posHeadOnly}`

// --- methods ---
/**
 * Download a corpus converted to the desired format.
 * @param corpus UUID of the corpus.
 * @param job Tagger job name.
 * @param format Use enum here.
 * @param posHeadOnly Whether to include only the head of the POS tags in the export.
 */
export function convertCorpus(corpus: UUID, job: string, format: Format, posHeadOnly: boolean): Promise<BlobResponse> {
    return Utils.getBlob(convertCorpusPath(corpus, job, format, posHeadOnly))
}

/**
 * Download a corpus converted to the desired format, merging any files of that format.
 * @param corpus UUID of the corpus.
 * @param job Tagger job name.
 * @param format Use enum here.
 * @param posHeadOnly Whether to include only the head of the POS tags in the export.
 */
export function mergeCorpus(corpus: UUID, job: string, format: Format, posHeadOnly: boolean): Promise<BlobResponse> {
    return Utils.getBlob(mergeCorpusPath(corpus, job, format, posHeadOnly))
}
