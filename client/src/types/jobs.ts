import { Tagger } from "@/types/taggers"
import { Term, WordForm } from "@/types/evaluation"

export const SOURCE_LAYER: string = "sourceLayer"

export type Job = {
    tagger: Tagger,
    progress: Progress,
    preview: LayerPreview,
    lastModified: number | null
}

export type Progress = {
    pending: number,
    processing: number,
    failed: number,
    finished: number,
    total: number,
    untagged: number,
    busy: boolean,
    hasError: boolean,
    errors: { [document: string]: string }
}

export type LayerPreview = {
    wordforms: WordForm[]
    terms: Term[]
}

export type LayerSummary = {
    tokens: number
}
