/**
 * API calls for exporting corpora and documents.
 * Either converted to a certain format or merged with their original file if the format supports it.
 */

import { getBlob, type BlobResponse } from "@/api/utils"
import type { UUID } from "@/types/corpora"
import type { Format } from "@/types/documents"

const convertCorpusPath = (corpus: UUID, job: string, format: Format, posHeadOnly: boolean): string =>
    `/corpora/${corpus}/layers/${job}/export/convert?format=${format}&posHeadOnly=${posHeadOnly}`
const mergeCorpusPath = (corpus: UUID, job: string, format: Format, posHeadOnly: boolean): string =>
    `/corpora/${corpus}/layers/${job}/export/merge?format=${format}&posHeadOnly=${posHeadOnly}`

/**
 * Download a corpus converted to the desired format.
 * @param corpus UUID of the corpus.
 * @param job Tagger job name.
 * @param format Use enum here.
 * @param posHeadOnly Whether to include only the head of the POS tags in the export.
 */
export function convertCorpus(corpus: UUID, job: string, format: Format, posHeadOnly: boolean): Promise<BlobResponse> {
    return getBlob(convertCorpusPath(corpus, job, format, posHeadOnly))
}

/**
 * Download a corpus converted to the desired format, merging any files of that format.
 * @param corpus UUID of the corpus.
 * @param job Tagger job name.
 * @param format Use enum here.
 * @param posHeadOnly Whether to include only the head of the POS tags in the export.
 */
export function mergeCorpus(corpus: UUID, job: string, format: Format, posHeadOnly: boolean): Promise<BlobResponse> {
    return getBlob(mergeCorpusPath(corpus, job, format, posHeadOnly))
}
