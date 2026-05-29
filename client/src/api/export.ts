/**
 * API calls for exporting corpora and documents.
 * Either converted to a certain format or merged with their original file if the format supports it.
 */

import { getBlob, type BlobResponse } from "@/api/utils"
import type { UUID } from "@/types/corpora"
import type { Format } from "@/types/documents"
import { endpoints } from "@/api"

/**
 * Download a corpus converted to the desired format.
 * @param corpus UUID of the corpus.
 * @param job Tagger job name.
 * @param format Use enum here.
 * @param posHeadOnly Whether to include only the head of the POS tags in the export.
 */
export function convertCorpus(
    corpus: UUID,
    layer: string,
    format: Format,
    posHeadOnly: boolean,
): Promise<BlobResponse> {
    return getBlob(endpoints.export.convert({ corpus, layer }, { format, posHeadOnly }))
}

/**
 * Download a corpus converted to the desired format, merging any files of that format.
 * @param corpus UUID of the corpus.
 * @param job Tagger job name.
 * @param format Use enum here.
 * @param posHeadOnly Whether to include only the head of the POS tags in the export.
 */
export function mergeCorpus(corpus: UUID, layer: string, format: Format, posHeadOnly: boolean): Promise<BlobResponse> {
    return getBlob(endpoints.export.merge({ corpus, layer }, { format, posHeadOnly }))
}
